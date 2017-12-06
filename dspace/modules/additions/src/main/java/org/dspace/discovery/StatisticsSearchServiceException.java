package org.dspace.discovery;

/**
* Exception used by discovery when discovery search exceptions occur
*
* @author Kevin Van de Velde (kevin at atmire dot com)
* @author Mark Diggory (markd at atmire dot com)
* @author Ben Bosman (ben at atmire dot com)
*/
public class StatisticsSearchServiceException extends Exception {

   public StatisticsSearchServiceException() {
   }

   public StatisticsSearchServiceException(String s) {
       super(s);
   }

   public StatisticsSearchServiceException(String s, Throwable throwable) {
       super(s, throwable);
   }

   public StatisticsSearchServiceException(Throwable throwable) {
       super(throwable);
   }
   
}