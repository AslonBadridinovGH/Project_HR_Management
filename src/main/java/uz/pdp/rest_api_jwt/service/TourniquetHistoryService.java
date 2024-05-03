package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.TourniquetCard;
import uz.pdp.rest_api_jwt.entity.TourniquetHistory;
import uz.pdp.rest_api_jwt.payload.TourniquetHistoryDto;
import uz.pdp.rest_api_jwt.repository.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

@Service
public class TourniquetHistoryService {

    @Autowired
    TourniquetHistoryRepository tourniquetHistoryRepository;

    @Autowired
    TourniquetCardRepository tourniquetCardRepository;


    public ApiResponse addTourniquetHistory(TourniquetHistoryDto historyDto){

        Optional<TourniquetCard> cardRepositoryById = tourniquetCardRepository.findById(historyDto.getCardId());
        if (!cardRepositoryById.isPresent())
            return new ApiResponse("Tourniquet not found",false);

         TourniquetHistory history=new TourniquetHistory();

         Date date1, date2;
         DateFormat dateFormat2=new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        try {
            date1=dateFormat2.parse(historyDto.getGetInTime());
            java.sql.Timestamp sql_Timestamp1=new java.sql.Timestamp(date1.getTime());
            history.setGetInTime(sql_Timestamp1);

            date2=dateFormat2.parse(historyDto.getGetOutTime());
            java.sql.Timestamp sql_Timestamp2=new java.sql.Timestamp(date2.getTime());
            history.setGetOutTime(sql_Timestamp2);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        history.setCard(cardRepositoryById.get());
        tourniquetHistoryRepository.save(history);
        return new ApiResponse("Tourniquet not saved",true);
    }

    public ApiResponse editTourniquetHistory(UUID id, TourniquetHistoryDto historyDto) {

        Optional<TourniquetHistory> optionalTurnKet = tourniquetHistoryRepository.findById(id);
        if (!optionalTurnKet.isPresent()) {
        return new ApiResponse("tourniquetHistory not found", false);}

         Optional<TourniquetCard> cardRepositoryById = tourniquetCardRepository.findById(historyDto.getCardId());
        if (!cardRepositoryById.isPresent())
            return new ApiResponse("tourniquetHistory not found",false);

         TourniquetHistory history = optionalTurnKet.get();

        Date date1, date2;
        DateFormat dateFormat2=new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        try {
            date1=dateFormat2.parse(historyDto.getGetInTime());
            java.sql.Timestamp sql_Timestamp1=new java.sql.Timestamp(date1.getTime());
            history.setGetInTime(sql_Timestamp1);

            date2=dateFormat2.parse(historyDto.getGetOutTime());
            java.sql.Timestamp sql_Timestamp2=new java.sql.Timestamp(date2.getTime());
            history.setGetOutTime(sql_Timestamp2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        history.setCard(cardRepositoryById.get());
        tourniquetHistoryRepository.save(history);
        return new ApiResponse("successfully changed", true);
   }

    public ApiResponse getTourniquetHistory(UUID id) {
        Optional<TourniquetHistory> optionalTurnKet = tourniquetHistoryRepository.findById(id);
        return optionalTurnKet.map(tourniquetHistory -> new ApiResponse("History found", true, tourniquetHistory)).orElseGet(() -> new ApiResponse("History not found", false));
    }

    public ApiResponse deleteTourniquetHistory(UUID id) {

        Optional<TourniquetHistory> optionalTurnKet = tourniquetHistoryRepository.findById(id);
        if (optionalTurnKet.isPresent())
            try {
                tourniquetHistoryRepository.deleteById(id);
                return new ApiResponse("TourniquetHistory deleted", true);
            } catch (Exception e) {
                return new ApiResponse("TourniquetHistory not deleted", false);
            }
        return new ApiResponse("TourniquetHistory not found", false);
    }

    public ApiResponse getTourniquetHistories() {
        List<TourniquetHistory> all = tourniquetHistoryRepository.findAll();
        return new ApiResponse("tourniquetHistory", true, all);

    }

}
