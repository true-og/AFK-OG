/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.integrations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.codecrafter.smartAfk.AFKOG;
import de.codecrafter.smartAfk.utils.AfkManager;

/**
 * Optional, reflection-based bridge to NoCheatPlus. When NoCheatPlus is present,
 * registers a passive hook on the NET_ATTACKFREQUENCY check that records when a
 * player is flagged for excessive attack frequency. The hook is observe-only: it
 * never cancels or alters NoCheatPlus' own violation processing.
 *
 * Reflection (rather than a compile-time dependency) keeps NoCheatPlus a true
 * soft dependency — the plugin compiles and runs identically whether or not
 * NoCheatPlus is installed.
 */
public final class NoCheatPlusHook {

	private static Integer registeredHookId;

	private NoCheatPlusHook() {

	}

	/**
	 * Registers the attack-frequency observer if NoCheatPlus is installed. A no-op
	 * (with a single warning) if NoCheatPlus is absent or its hook API is
	 * incompatible.
	 */
	public static void register(AFKOG plugin, AfkManager afkManager) {

		final Plugin ncp = Bukkit.getPluginManager().getPlugin("NoCheatPlus");
		if (ncp == null) {

			return;

		}

		try {

			final ClassLoader loader = ncp.getClass().getClassLoader();
			final Class<?> hookManagerClass = Class.forName("fr.neatmonster.nocheatplus.hooks.NCPHookManager", true, loader);
			final Class<?> hookClass = Class.forName("fr.neatmonster.nocheatplus.hooks.NCPHook", true, loader);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Class<? extends Enum> checkTypeClass = (Class<? extends Enum>) Class.forName("fr.neatmonster.nocheatplus.checks.CheckType", true, loader);
			@SuppressWarnings("unchecked")
			final Object attackFrequency = Enum.valueOf(checkTypeClass, "NET_ATTACKFREQUENCY");

			final String version = plugin.getPluginMeta().getVersion();
			final Object hookProxy = Proxy.newProxyInstance(loader, new Class<?>[] { hookClass }, new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args) {

					switch (method.getName()) {

						case "getHookName":
							return "AFK-OG";
						case "getHookVersion":
							return version;
						case "onCheckFailure":
							if (args != null && args.length >= 2 && args[1] instanceof Player player) {

								afkManager.flagAttackFrequency(player);

							}
							// false = do not cancel NoCheatPlus' failure processing.
							return Boolean.FALSE;
						case "equals":
							return proxy == args[0];
						case "hashCode":
							return System.identityHashCode(proxy);
						case "toString":
							return "AFK-OG NCPHook";
						default:
							return null;

					}

				}

			});

			final Method addHook = hookManagerClass.getMethod("addHook", checkTypeClass, hookClass);
			registeredHookId = (Integer) addHook.invoke(null, attackFrequency, hookProxy);

			plugin.getLogger().info("Hooked into NoCheatPlus (NET_ATTACKFREQUENCY) for combat auto-click detection.");

		} catch (final ReflectiveOperationException | RuntimeException exception) {

			plugin.getLogger().warning("NoCheatPlus is present but its hook API was unavailable; combat auto-click will rely on cadence only (" + exception.getClass().getSimpleName() + ").");

		}

	}

	/**
	 * Removes the hook registered by {@link #register} so that a plugin
	 * disable/reload does not leave a stale proxy (and the old plugin
	 * classloader) retained inside NoCheatPlus. No-op if nothing was registered.
	 */
	public static void unregister(AFKOG plugin) {

		if (registeredHookId == null) {

			return;

		}

		final Plugin ncp = Bukkit.getPluginManager().getPlugin("NoCheatPlus");
		if (ncp == null) {

			registeredHookId = null;
			return;

		}

		try {

			final ClassLoader loader = ncp.getClass().getClassLoader();
			final Class<?> hookManagerClass = Class.forName("fr.neatmonster.nocheatplus.hooks.NCPHookManager", true, loader);
			final Method removeHook = hookManagerClass.getMethod("removeHook", Integer.class);
			removeHook.invoke(null, registeredHookId);

		} catch (final ReflectiveOperationException | RuntimeException exception) {

			plugin.getLogger().warning("Failed to unregister NoCheatPlus hook (" + exception.getClass().getSimpleName() + ").");

		} finally {

			registeredHookId = null;

		}

	}

}
