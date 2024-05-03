package uz.pdp.rest_api_jwt.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.rest_api_jwt.entity.Company;
import uz.pdp.rest_api_jwt.repository.CompanyRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyService {

    @Autowired
    CompanyRepository companyRepository;

    public ApiResponse getCompany() {
        List<Company> optionalCompany = companyRepository.findAll();
        return new ApiResponse("Company not found", true, optionalCompany);
    }

    public ApiResponse addCompany(Company company){
        companyRepository.save(company);
        return new ApiResponse("successfully saved",true);
    }

    public ApiResponse editCompany(UUID id, Company company) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isPresent()) {
            companyRepository.save(company);
        return new ApiResponse("Company changed", true);
        }
        return new ApiResponse("Company not changed", false);
    }

    public ApiResponse deleteCompany(UUID id) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        if (optionalCompany.isPresent()){
            try {
                companyRepository.deleteById(id);
                return new ApiResponse("Company removed", true);
            } catch (Exception e) {   // Foreign Key
                return new ApiResponse("Company not removed", false);
            }
        }
        return new ApiResponse("Company not found", false);
    }


        public ApiResponse getCompanyById(UUID id) {
        Optional<Company> optionalCompany = companyRepository.findById(id);
        return optionalCompany.map(company -> new ApiResponse("Company ",true, company)
        ).orElseGet(() -> new ApiResponse("Company not found", false, null));
     }


}
