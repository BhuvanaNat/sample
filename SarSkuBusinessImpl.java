/**
 * 
 */
package org.smartbuy.sar.SarRest.business;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.smartbuy.sar.SarRest.DAO.SarSkuDao;
import org.smartbuy.sar.SarRest.DTO.SarSkuDTO;
import org.smartbuy.sar.SarRest.DTO.SarSkuDTOList;
import org.smartbuy.sar.SarRest.DTO.ToBeApprovedSku;
import org.smartbuy.sar.SarRest.DTO.ToBeApprovedSkuList;
import org.smartbuy.sar.SarRest.util.JMSMessage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author User
 *
 */
public class SarSkuBusinessImpl implements SarSkuBusiness {

	private static final Logger logger = Logger
			.getLogger(SarSkuBusinessImpl.class);
	private SarSkuDao sarSkuDao;
	private XStream xstream;
	private JMSMessage jmsMessage;
	private SarSkuDTOList getReplenishOrderList;
	private SarSkuDTOList getUpdateOrderList;
	
	private ToBeApprovedSkuList toBeApprovedList = new ToBeApprovedSkuList();;
	private int lastPoNumber;
	private int newPoNumber;
	int getApproval = 0;
	
	String getApprovalMessage = null;

	public SarSkuBusinessImpl() {
		sarSkuDao = new SarSkuDao();
		xstream = new XStream(new StaxDriver());
		xstream.autodetectAnnotations(true);
		xstream.processAnnotations(ArrayList.class);
		xstream.processAnnotations(SarSkuDTO.class);
		xstream.processAnnotations(SarSkuDTOList.class);
		xstream.processAnnotations(ToBeApprovedSku.class);
		xstream.processAnnotations(ToBeApprovedSkuList.class);
	}

	/**
	 * Returns the latest orderList from the database as XML.
	 * 
	 * @return String
	 */
	@Override
	public String getReplenishOrder() {
		try {
			getReplenishOrderList = sarSkuDao.getReplenishOrder();
		} catch (Exception e) {
			logger.error("Exception thrown in getReplenishOrder method ", e);
		}
		logger.info("SarSku list successfully retrieved from database");
		return xstream.toXML(getReplenishOrderList);
	}

	/**
	 * Updates the order
	 * 
	 * @param updateSarSku
	 * @return String
	 */
	@Override
	public String updateOrder(String updateSarSku) {
		String message = null;

		getUpdateOrderList = (SarSkuDTOList) xstream.fromXML(updateSarSku);
		
		List<SarSkuDTO> sarList = getUpdateOrderList.getSarSkuDTOList();
		
		newPoNumber = generateNewPoNumber();
		logger.info("new po number generated");
		
		for(SarSkuDTO skuDto: sarList){
			int skuNumber = skuDto.getSkuNumber();
			int updatedOrderQty = skuDto.getOrderQuantity();
			int originalOrderQty = sarSkuDao
					.getOrderQtyBySkuNumber(skuNumber);
			boolean withinRange = isUpdatedOrderWithinRange(originalOrderQty,
					updatedOrderQty);
			
			logger.info("The updated order quantity is within +  or - 10%: "
					+ withinRange);
			
			if (withinRange) {
				sarSkuDao.updateOrder(newPoNumber, updatedOrderQty, skuNumber);
				message = "successfully UPDATED the order";
			}
			
			else {
				getApproval++;
				toBeApprovedList.add(new ToBeApprovedSku(skuNumber, skuDto.getSkuDescription(), originalOrderQty, updatedOrderQty));
			}
			
			if(getApproval > 0){
				getApprovalMessage = xstream.toXML(getApproval);
				jmsMessage = new JMSMessage();
				jmsMessage.sendMessage();
				message = "Unable to change order quantity for some sku's. ";
			}
		}
		return message;
	}

	/**
	 * Calculates updated order quantity is within +10% or -10% of the original
	 * order quantity
	 * 
	 * @param originalOrderQty
	 * @param updatedOrderQty
	 * @return boolean
	 */
	public boolean isUpdatedOrderWithinRange(int originalOrderQty,
			int updatedOrderQty) {
		int orderDifference = Math.abs(originalOrderQty - updatedOrderQty);
		int orderPercent = (orderDifference * 100) / originalOrderQty;

		if (orderPercent >= 10) {
			return false;
		}
		return true;
	}

	public int generateNewPoNumber() {
		lastPoNumber = sarSkuDao.getLastPoNumber();
		newPoNumber = lastPoNumber + 1;
		sarSkuDao.updatePoNumTable(newPoNumber);
		return newPoNumber;
	}

}
