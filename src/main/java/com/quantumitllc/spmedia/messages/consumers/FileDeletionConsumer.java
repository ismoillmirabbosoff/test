//package com.crasoftinc.clnmedia.messages.consumers;
//
//import com.crasoftinc.clnmedia.messages.constants.Queues;
//import com.crasoftinc.clnmedia.messages.models.FileDeletedMessage;
//import com.crasoftinc.clnmedia.services.CustomS3Service;
//import java.util.List;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class FileDeletionConsumer {
//  private final CustomS3Service s3;
//
//  public FileDeletionConsumer(CustomS3Service s3) {
//    this.s3 = s3;
//  }
//
//  @RabbitListener(queues = Queues.FILE_FOR_DELETE_QUEUE)
//  public void receive(FileDeletedMessage message){
//    try {
//      log.info("Received Event: " + message);
//      if(message.isPermanent()) {
//        List<String> filesList = message.getFiles();
//        String[] files = new String[filesList.size()];
//        filesList.toArray(files);
//        s3.deleteMultipleObjects(files);
//      }else{
////        TODO: implement version delete if we apply versioning in AWS
//      }
//      log.info("Processed Event: " + message);
//    }catch(Exception ex){
//      log.error("Error processing event: " + message + "\n" + ex.getLocalizedMessage());
//    }
//  }
//}
