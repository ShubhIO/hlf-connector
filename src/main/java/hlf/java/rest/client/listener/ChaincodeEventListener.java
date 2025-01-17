package hlf.java.rest.client.listener;

import static hlf.java.rest.client.util.FabricEventParseUtil.createEventStructure;

import hlf.java.rest.client.model.EventType;
import hlf.java.rest.client.service.EventPublishService;
import hlf.java.rest.client.util.FabricClientConstants;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "fabric.events", name = "enable", havingValue = "true")
public class ChaincodeEventListener {
  @Autowired private EventPublishService eventPublishService;

  private static String eventTxnId = FabricClientConstants.FABRIC_TRANSACTION_ID;

  public void listener(
      String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent, String channelName) {
    String es = blockEvent.getPeer() != null ? blockEvent.getPeer().getName() : "peer was null!!!";

    synchronized (this) {
      if (!chaincodeEvent.getTxId().equalsIgnoreCase(eventTxnId)) {

        log.info("Chaincode ID: {}", chaincodeEvent.getChaincodeId());
        log.info("Event Name: {}", chaincodeEvent.getEventName());
        log.info("Transaction ID: {}", chaincodeEvent.getTxId());
        log.info("Payload: {}", new String(chaincodeEvent.getPayload()));
        log.info("Event Source: {}", es);
        log.info("Channel Name: {}", channelName);
        eventPublishService.publishChaincodeEvents(
            createEventStructure(
                new String(chaincodeEvent.getPayload()),
                "",
                chaincodeEvent.getTxId(),
                blockEvent.getBlockNumber(),
                EventType.CHAINCODE_EVENT),
            chaincodeEvent.getTxId(),
            chaincodeEvent.getEventName(),
            channelName);
        eventTxnId = chaincodeEvent.getTxId();
      } else {
        log.debug("Duplicate Transaction; ID: {}", chaincodeEvent.getTxId());
      }
    }
  }
}
