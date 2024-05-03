package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Company;
import uz.pdp.rest_api_jwt.entity.Employee;
import uz.pdp.rest_api_jwt.entity.TourniquetCard;
import uz.pdp.rest_api_jwt.payload.TourniquetCardDto;
import uz.pdp.rest_api_jwt.repository.CompanyRepository;
import uz.pdp.rest_api_jwt.repository.EmployeeRepository;
import uz.pdp.rest_api_jwt.repository.TourniquetCardRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class TourniquetCardService {

    @Autowired
    TourniquetCardRepository tourniquetCardRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    EmployeeRepository employeeRepository;


    private static final Integer ACTUAL=1;
    private static final Integer EXPIRED=2;


    public ApiResponse getTourniquetCardById(UUID id) {

        Optional<TourniquetCard> optionalTourniquetCard = tourniquetCardRepository.findById(id);
        if (!optionalTourniquetCard.isPresent()){
            return new ApiResponse("TurnKet not found", false);
        }
        return new ApiResponse("TurnKet found", true, optionalTourniquetCard.get());
     /*
              return optionalTourniquetCard.map ( tourniquetCard -> new ApiResponse("TurnKet found",   true, tourniquetCard))
                                     .orElseGet ( ()             -> new ApiResponse("TurnKet not found", false  ));
     */
    }

    public ApiResponse addTourniquetCard(TourniquetCardDto turnKetDto){

         TourniquetCard turnKet=new TourniquetCard();
        turnKet.setExpireDate(turnKetDto.getExpireDate());

      Optional<Company> optionalCompany = companyRepository.findById(turnKetDto.getCompanyId());
      if (!optionalCompany.isPresent())
      return new ApiResponse("company not found", false);

      turnKet.setCompany(optionalCompany.get());
      Optional<Employee> optionalEmployee = employeeRepository.findById(turnKetDto.getEmployeeId());

      if (!optionalEmployee.isPresent())
      return new ApiResponse("employee not found", false);
      turnKet.setEmployee(optionalEmployee.get());

      if (Period.between(LocalDate.now(),turnKetDto.getExpireDate()).getDays() >=0)
      {  turnKet.setStatus(ACTUAL);}
      else if (Period.between(LocalDate.now(),turnKetDto.getExpireDate()).getDays()<0)
      {turnKet.setStatus(EXPIRED);}

      tourniquetCardRepository.save(turnKet);
      return new ApiResponse("Successfully saved ",true);
    }

    public ApiResponse editTourniquetCard(UUID id, TourniquetCardDto turnKetDto) {

         Optional<TourniquetCard> optionalTurnKet = tourniquetCardRepository.findById(id);
        if (!optionalTurnKet.isPresent()) {
            return new ApiResponse("tourniquet not found", false); }

         Optional<Company> optionalCompany = companyRepository.findById(turnKetDto.getCompanyId());
        if (!optionalCompany.isPresent())
            return new ApiResponse("company not found", false);

         Optional<Employee> optionalEmployee = employeeRepository.findById(turnKetDto.getEmployeeId());
        if (!optionalEmployee.isPresent())
            return new ApiResponse("employee not found", false);

         TourniquetCard turnKet = optionalTurnKet.get();

        turnKet.setExpireDate(turnKetDto.getExpireDate());
        turnKet.setCompany(optionalCompany.get());
        turnKet.setEmployee(optionalEmployee.get());

        if (Period.between(LocalDate.now(),turnKetDto.getExpireDate()).getDays() >=0)
        {  turnKet.setStatus(ACTUAL);}
        else if (Period.between(LocalDate.now(),turnKetDto.getExpireDate()).getDays()<0)
        {turnKet.setStatus(EXPIRED);}

        tourniquetCardRepository.save(turnKet);
        return new ApiResponse("successfully changed", true);
    }

    public ApiResponse deleteTourniquetCard(UUID id) {

        Optional<TourniquetCard> optionalTurnKet = tourniquetCardRepository.findById(id);
        if (optionalTurnKet.isPresent())
            try {
                tourniquetCardRepository.deleteById(id);
                return new ApiResponse("TurnKet deleted", true);
            } catch (Exception e) {
                return new ApiResponse("TurnKet not deleted", false);
            }
        return new ApiResponse("TurnKet not found", false);
    }

    public ApiResponse getTourniquetCards() {
        List<TourniquetCard> all = tourniquetCardRepository.findAll();
        return new ApiResponse(Collections.singletonList(all),true,"tourniquetCards");
    }

}


/*
     turnKet.setGetIn(turnKetDto.isGetIn());
     turnKet.setGetInTime(turnKetDto.getGetInTime());
     turnKet.setGetOutTime(turnKetDto.getGetOutTime());
     turnKet.setGetOut(turnKetDto.isGetOut());
     editingTurnKet.setGetIn(turnKetDto.isGetIn());
     editingTurnKet.setGetInTime(turnKetDto.getGetInTime());
     editingTurnKet.setGetOutTime(turnKetDto.getGetOutTime());
     editingTurnKet.setGetOut(turnKetDto.isGetOut());
   */