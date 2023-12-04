package com.jubiman.devious.forge.v1_20_1.deviouseconomy.database;

import com.jubiman.devious.forge.v1_20_1.deviouseconomy.Config;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.DeviousEconomy;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousCapManager;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.capability.DeviousPlayer;
import com.jubiman.devious.forge.v1_20_1.deviouseconomy.enums.Rank;

import java.sql.*;
import java.util.UUID;

public class DatabaseConnection {
	private Connection connection;

	/**
	 * Creates a new database connection.
	 */
	public DatabaseConnection() {
		// TODO: Set reconnect interval
		try {
			connect();
		} catch (SQLException | ClassNotFoundException e) {
			DeviousEconomy.LOGGER.warn("Failed to connect to DB", e);
		}
	}

	/**
	 * Cleans all dirty players.
	 */
	public static void cleanDirtyPlayers() {
		DatabaseConnection db = DeviousEconomy.getInstance().getDatabase();
		for (DeviousPlayer player : DeviousCapManager.getDirtyPlayers()) {
			DeviousEconomy.LOGGER.debug("Cleaning dirty player " + player.getUUID());
			db.setRank(player.getUUID(), player.rank);
			db.setRank(player.getUUID(), player.rank);
			db.setCoins(player.getUUID(), player.coins);
		}
	}

	/**
	 * Connects to the database.
	 * @throws SQLException If the connection fails.
	 * @throws ClassNotFoundException If the driver class is not found.
	 */
	public void connect() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		// TODO: support URL encoding?
		String url = "jdbc:postgresql://" + Config.database.getHostname()
				+ ":" + Config.database.getPort() + "/" + Config.database.getDatabase();
		DeviousEconomy.LOGGER.info("Connecting to DB at " + url);
		connection = DriverManager.getConnection(url,
				Config.database.getUsername(), Config.database.getPassword()
		);

		setupTables();
	}

	/**
	 * Sets up the tables in the database.
	 */
	private void setupTables() {
		// TableName | ColumnName
		// PlayerInfo | UUID | rank | coins
		try (PreparedStatement statement = connection.prepareStatement("""
					CREATE TABLE IF NOT EXISTS PlayerInfo (UUID VARCHAR(36) PRIMARY KEY,
					rank VARCHAR(16) NOT NULL,
					coins INT CHECK (coins >= 0))
				""")) {
			statement.execute();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to create table Devious", e);
		}
	}

	/**
	 * Adds a player to the database, with default values defined in config.
	 * @param uuid The UUID of the player.
	 */
	public void addPlayer(UUID uuid) {
		if (connection == null) return;

		try (PreparedStatement statement = connection.prepareStatement("INSERT INTO PlayerInfo (UUID, rank, coins) VALUES (?, ?, ?) ON CONFLICT DO NOTHING")) {
			statement.setString(1, uuid.toString());
			statement.setString(2, "DEFAULT"); // TODO: Change
			statement.setInt(3, Config.ranks.getStarterCoins());
			statement.execute();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to add player " + uuid + " to DB", e);
		}
	}

	/**
	 * Sets the rank of a player.
	 * @param uuid The UUID of the player.
	 * @param rank The rank to set.
	 */
	public void setRank(UUID uuid, Rank rank) {
		try (PreparedStatement statement = connection.prepareStatement("UPDATE PlayerInfo SET rank = ? WHERE UUID = ?")) {
			statement.setString(1, rank.toString());
			statement.setString(2, uuid.toString());
			statement.execute();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to set rank of player " + uuid + " to " + rank, e);
		}
	}

	/**
	 * Sets the amount of coins a player has.
	 * @param uuid The UUID of the player.
	 * @param coins The amount of coins to set.
	 */
	public void setCoins(UUID uuid, int coins) {
		try (PreparedStatement statement = connection.prepareStatement("UPDATE PlayerInfo SET coins = ? WHERE UUID = ?")) {
			statement.setInt(1, coins);
			statement.setString(2, uuid.toString());
			statement.execute();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to set coins of player " + uuid + " to " + coins, e);
		}
	}

	/**
	 * Returns the rank of a player.
	 * @param uuid The UUID of the player.
	 * @return The rank of the player.
	 */
	public Rank getRank(UUID uuid) {
		try (PreparedStatement statement = connection.prepareStatement("SELECT rank FROM PlayerInfo WHERE UUID = ?")) {
			statement.setString(1, uuid.toString());
			statement.execute();
			ResultSet rs = statement.getResultSet();
			if (!rs.next()) return Rank.DEFAULT;
			return Rank.valueOf(rs.getString("rank").toUpperCase());
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to get rank of player " + uuid, e);
		}
		return null;
	}

	/**
	 * Returns the amount of coins a player has.
	 * @param uuid The UUID of the player.
	 * @return The amount of coins the player has.
	 */
	public int getCoins(UUID uuid) {
		try (PreparedStatement statement = connection.prepareStatement("SELECT coins FROM PlayerInfo WHERE UUID = ?")) {
			statement.setString(1, uuid.toString());
			// Print the query
			DeviousEconomy.LOGGER.debug(statement.toString());
			statement.execute();
			ResultSet rs = statement.getResultSet();
			return (rs.next() ? 1 : 0) * rs.getInt("coins");
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.debug("Failed to get coins of player " + uuid, e);
			throw new IllegalArgumentException("Player not found!");
		}
	}

	/**
	 * Removes coins from a player's balance.
	 * @param uuid The UUID of the player.
	 * @param coins The amount of coins to remove.
	 */
	public void purchase(UUID uuid, int coins) {
		try (PreparedStatement statement = connection.prepareStatement("UPDATE PlayerInfo SET coins = coins - ? WHERE UUID = ?")) {
			statement.setInt(1, coins);
			statement.setString(2, uuid.toString());
			statement.execute();
		} catch (SQLException e) {
			// Check if the exception was caused by insufficient funds
			if (e.getMessage().contains("playerinfo_coins_check")) {
				throw new IllegalArgumentException("Insufficient funds");
			} else {
				DeviousEconomy.LOGGER.error("Failed to purchase " + coins + " coins for player " + uuid, e);
			}
		}
	}

	/**
	 * Adds coins to a player's balance.
	 * @param uuid The UUID of the player.
	 * @param coins The amount of coins to add.
	 */
	public void sell(UUID uuid, int coins) {
		// Add coins to player's balance
		try (PreparedStatement statement = connection.prepareStatement("UPDATE PlayerInfo SET coins = coins + ? WHERE UUID = ?")) {
			statement.setInt(1, coins);
			statement.setString(2, uuid.toString());
			statement.execute();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.error("Failed to sell " + coins + " coins for player " + uuid, e);
		}
	}

	/**
	 * Returns the connection to the database.
	 * @return The connection to the database.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Closes the connection to the database.
	 */
	public void close() {
		if (connection == null) return;
		try {
			if (!connection.getAutoCommit()) {
				connection.rollback();
			}
			DeviousEconomy.LOGGER.info("Closing DB connection to " + connection.getMetaData().getDatabaseProductName());
			connection.close();
		} catch (SQLException e) {
			DeviousEconomy.LOGGER.warn("Can't cleanly shut down DB connection: " + e.getMessage() +". See debug logs for more info.");
			DeviousEconomy.LOGGER.debug("Can't cleanly shut down DB connection", e);
		}
	}

	/**
	 * Reconnects to the database.
	 */
	public void reconnect() {
		try {
			close();
			connect();
		} catch (SQLException | ClassNotFoundException e) {
			DeviousEconomy.LOGGER.warn("Failed to reconnect to DB", e);
		}
	}
}
