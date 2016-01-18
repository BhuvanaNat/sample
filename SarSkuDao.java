package org.smartbuy.sar.SarRest.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.smartbuy.sar.SarRest.DTO.SarSkuDTO;
import org.smartbuy.sar.SarRest.DTO.SarSkuDTOList;

public class SarSkuDao {

	private static final Logger logger = Logger.getLogger(SarSkuDao.class);
	private SarSkuDTOList orderList = new SarSkuDTOList();;
	private Map<Integer, String> skuDescription = new HashMap<Integer, String>();
	private Connection connection;

	private int orderQty;
	private int isApproved = 1;
	private int poNumber = -1;
	
	private static String getLastPoNumberQuery = "SELECT * FROM PO_NUMBER";
	private static String getReplenishOrderQuery = "SELECT * FROM SAR_PO WHERE PO_NUMBER=?";
	private static String generateSkuDescriptionQuery = "SELECT SKU_NUMBER, SKU_DESC FROM SKU";
	private static String updateOrderQuery = "UPDATE SAR_PO SET PO_NUMBER = ?, IS_APPROVED = ?, ORDER_QTY = ? WHERE SKU_NUMBER = ?";
	private static String getOrderQtyBySkuNumberQuery = "SELECT ORDER_QTY FROM SAR_PO WHERE SKU_NUMBER = ?";
	private static String updatePoNumTableQuery = "UPDATE PO_NUMBER SET LAST_PO_NUM = ?";

	

	public SarSkuDao() {
		this.connection = DbConnection.createConnection();
	}

	public SarSkuDTOList getReplenishOrder() {
		poNumber = getLastPoNumber();
		generateSkuDescription();

		try {
			PreparedStatement statement = connection
					.prepareStatement(getReplenishOrderQuery);
			statement.setInt(1, poNumber);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				int skuNumber = result.getInt("SKU_NUMBER");
				orderList.getSarSkuDTOList().add(new SarSkuDTO(skuNumber, skuDescription
						.get(skuNumber), result.getInt("ORDER_QTY")));
			}
		} catch (SQLException e) {
			logger.error("Exception thrown in getReplenishOrder method ", e);

		}
		return orderList;
	}

	
	/**
	 * Generate skuDescription as Map<"skuNumber","skuDescription">
	 */
	public void generateSkuDescription() {
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement
					.executeQuery(generateSkuDescriptionQuery);
			while (result.next()) {
				skuDescription.put(result.getInt("SKU_NUMBER"),
						result.getString("SKU_DESC"));
			}
		} catch (SQLException e) {
			logger.error("Exception thrown in getSkuDescription method ", e);
		}
	}

	public void updateOrder(int poNumber, int updatedOrderQty, int skuNumber) {
		try {
			logger.info("Inside sarSkuDao updateOrder method");
			PreparedStatement statement = connection
					.prepareStatement(updateOrderQuery);
			statement.setString(1, Integer.toString(poNumber));
			statement.setInt(2, isApproved);
			statement.setInt(3, updatedOrderQty);
			statement.setInt(4, skuNumber);
			statement.executeUpdate();

		} catch (SQLException e) {
			logger.error("Exception thrown in updateOrder method ", e);
		}		
		logger.info("Successfully created new PO order");
	}

	public int getOrderQtyBySkuNumber(int skuNumber) {
		try {
			PreparedStatement statement = connection
					.prepareStatement(getOrderQtyBySkuNumberQuery);
			statement.setInt(1, skuNumber);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				orderQty = result.getInt("ORDER_QTY");
			}
		} catch (SQLException e) {
			logger.error("Exception thrown in getOrderQtyBySkuNumber method ",
					e);
		}
		return orderQty;
	}
	
	public int getLastPoNumber() {
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getLastPoNumberQuery);
			while (result.next()) {
				poNumber = result.getInt("LAST_PO_NUM");
			}
		} catch (SQLException e) {
			logger.error("Exception in getLastPoNumber method", e);
		}
		logger.info("Last po number is "+poNumber);
		return poNumber;
	}

	
	public void updatePoNumTable(int poNumber) {
		try {
			PreparedStatement statement = connection
					.prepareStatement(updatePoNumTableQuery);
			statement.setInt(1, poNumber);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Exception thrown in updateOrder method ", e);
		}
	}
}