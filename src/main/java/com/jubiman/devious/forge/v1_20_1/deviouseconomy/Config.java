package com.jubiman.devious.forge.v1_20_1.deviouseconomy;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums.Rank;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Random;

public final class Config {
	private static final Random random = new Random();
	static final ForgeConfigSpec SPEC;
	private static final ForgeConfigSpec.ConfigValue<String> identifier;
	private static final ForgeConfigSpec.IntValue reconnectInterval;

	public static final Database database;
	public static final class Database {
		private final ForgeConfigSpec.ConfigValue<String> hostname;
		private final ForgeConfigSpec.IntValue port;
		private final ForgeConfigSpec.ConfigValue<String> username;
		private final ForgeConfigSpec.ConfigValue<String> password;
		private final ForgeConfigSpec.ConfigValue<String> database;

		Database(ForgeConfigSpec.Builder builder) {
			builder.comment("The hostname of the database to connect to.");
			hostname = builder.define("hostname", "localhost");
			builder.comment("The port of the database to connect to.");
			port = builder.defineInRange("port", 5432, 0, 65535);
			builder.comment("The username to use when connecting to the database.");
			username = builder.define("username", "root");
			builder.comment("The password to use when connecting to the database.");
			password = builder.define("password", "password");
			builder.comment("The database to use when connecting to the database.");
			database = builder.define("database", "devious");
		}

		public String getHostname() {
			return hostname.get();
		}

		public int getPort() {
			return port.get();
		}

		public String getUsername() {
			return username.get();
		}

		public String getPassword() {
			return password.get();
		}

		public String getDatabase() {
			return database.get();
		}
	}

	public static final Ranks ranks;
	public static class Ranks {
		private final ForgeConfigSpec.ConfigValue<Integer> starterCoins;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropIntervalDefault;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropIntervalAdmin;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropIntervalModerator;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropIntervalSupporter;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountDefaultMin;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountDefaultMax;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountAdminMin;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountAdminMax;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountModeratorMin;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountModeratorMax;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountSupporterMin;
		private final ForgeConfigSpec.ConfigValue<Integer> coinDropAmountSupporterMax;

		public Ranks(ForgeConfigSpec.Builder builder) {
			builder.comment("The amount of coins a player gets when they first join the server.");
			starterCoins = builder.define("starter_coins", 100);
			builder.push("coins");
			{
				builder.push("interval");
				{
					builder.comment("The minimum interval in seconds to wait before dropping coins for a player.");
					coinDropIntervalDefault = builder.defineInRange("default", 300, 60, Integer.MAX_VALUE);
					coinDropIntervalAdmin = builder.defineInRange("admin", 60, 60, Integer.MAX_VALUE);
					coinDropIntervalModerator = builder.defineInRange("moderator", 120, 60, Integer.MAX_VALUE);
					coinDropIntervalSupporter = builder.defineInRange("supporter", 180, 60, Integer.MAX_VALUE);
					builder.pop();
				}
				builder.push("amount");
				{
					builder.comment("The amount of coins to drop for a player.");
					coinDropAmountDefaultMin = builder.define("defaultMin", 100);
					coinDropAmountDefaultMax = builder.define("defaultMax", 150);
					coinDropAmountAdminMin = builder.define("adminMin", 1000);
					coinDropAmountAdminMax = builder.define("adminMax", 2000);
					coinDropAmountModeratorMin = builder.define("moderatorMin", 200);
					coinDropAmountModeratorMax = builder.define("moderatorMax", 300);
					coinDropAmountSupporterMin = builder.define("supporterMin", 200);
					coinDropAmountSupporterMax = builder.define("supporterMax", 300);
					builder.pop();
				}
				builder.pop();
			}
		}

		public int getStarterCoins() {
			return starterCoins.get();
		}

		public long getCoinDropInterval(Rank rank) {
			return switch (rank) {
				case ADMIN -> coinDropIntervalAdmin.get();
				case MOD -> coinDropIntervalModerator.get();
				case SUPPORTER -> coinDropIntervalSupporter.get();
				default -> coinDropIntervalDefault.get();
			};
		}

		public int getCoinDropAmount(Rank rank) {
			// Slightly randomize the amount of coins dropped
			int min = switch (rank) {
				case ADMIN -> coinDropAmountAdminMin.get();
				case MOD -> coinDropAmountModeratorMin.get();
				case SUPPORTER -> coinDropAmountSupporterMin.get();
				default -> coinDropAmountDefaultMin.get();
			};
			int max = switch (rank) {
				case ADMIN -> coinDropAmountAdminMax.get();
				case MOD -> coinDropAmountModeratorMax.get();
				case SUPPORTER -> coinDropAmountSupporterMax.get();
				default -> coinDropAmountDefaultMax.get();
			};
			return random.nextInt(min, max + 1);
		}
	}

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		{
			builder.push("server");
			{
				builder.comment("The identifier (aka server name) to use when connecting to the server.");
				identifier = builder.define("identifier", "UNKNOWN");
				builder.comment("The interval in seconds to wait before reconnecting to the Devious Socket.");
				// 5 minutes default, 1 second minimum, 1 hour maximum
				reconnectInterval = builder.defineInRange("reconnect_interval", 5 * 60, 1, 60 * 60);
				builder.pop();
			}
			builder.push("database");
			{
				database = new Database(builder);
				builder.pop();
			}
			builder.push("ranks");
			{
				ranks = new Ranks(builder);
				builder.pop();
			}
		}
		SPEC = builder.build();
	}

	public static String getIdentifier() {
		return identifier.get();
	}
	public static int getReconnectInterval() {
		return reconnectInterval.get();
	}
}
