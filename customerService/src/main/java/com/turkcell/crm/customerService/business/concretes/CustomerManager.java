package com.turkcell.crm.customerService.business.concretes;

import com.turkcell.crm.customerService.business.dtos.requests.Customer.UpdateIndividualCustomerRequest;
import com.turkcell.crm.customerService.dataAccess.abstracts.CustomerRepository;
import com.turkcell.crm.customerService.business.abstracts.CustomerService;
import com.turkcell.crm.customerService.business.dtos.responses.Customer.CreatedCustomerResponse;
import com.turkcell.crm.customerService.business.dtos.responses.Customer.GetAllCustomerResponse;
import com.turkcell.crm.customerService.business.dtos.responses.Customer.GetCustomerResponseById;
import com.turkcell.crm.customerService.business.dtos.responses.Customer.UpdatedCustomerResponse;
import com.turkcell.crm.customerService.business.rules.CustomerBusinessRules;
import com.turkcell.crm.customerService.entities.concretes.Customer;
import com.turkcell.crm.customerService.core.utilities.mapping.ModelMapperService;
import com.turkcell.crm.customerService.entities.concretes.IndividualCustomer;
import com.turkcell.crm.customerService.kafka.producers.CustomerProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.turkcell.crm.common.events.identity.CreateCustomerRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class CustomerManager implements CustomerService {

    private CustomerRepository customerRepository;
    private ModelMapperService modelMapperService;
    private CustomerBusinessRules customerBusinessRules;
    private CustomerProducer customerProducer;

    @Override
    public CreatedCustomerResponse add(CreateCustomerRequest createCustomerRequest) {
        customerBusinessRules.customerNameCannotBeDuplicated(createCustomerRequest.getFirstName()); //todo: isme göre değil, kimlik no'ya göre olsun
        IndividualCustomer customer = this.modelMapperService.forRequest().map(createCustomerRequest, IndividualCustomer.class);
        customer.setCreatedDate(LocalDateTime.now());

        IndividualCustomer savedCustomer = customerRepository.save(customer);

        CreateCustomerRequest createdCustomerRequest =
                this.modelMapperService.forResponse().map(savedCustomer, CreateCustomerRequest.class);

        customerProducer.sendMessage(createdCustomerRequest);
        return modelMapperService.forResponse().map(savedCustomer, CreatedCustomerResponse.class);
    }

    @Override
    public List<GetAllCustomerResponse> getAll() {
        List<Customer> customers = customerRepository.findAll();
        List<GetAllCustomerResponse> getCustomerResponseList = new ArrayList<>();
        for (Customer customer : customers) {
            GetAllCustomerResponse getCustomerResponse = this.modelMapperService.forResponse().map(customer, GetAllCustomerResponse.class);
            getCustomerResponseList.add(getCustomerResponse);
        }
        return getCustomerResponseList;
    }

    @Override
    public GetCustomerResponseById getById(int id) {
        customerBusinessRules.customerMustExists(id);
        Customer customer = customerRepository.findById(id).orElse(null);
        return this.modelMapperService.forResponse().map(customer, GetCustomerResponseById.class);
    }

    @Override
    public UpdatedCustomerResponse updateIndividual(UpdateIndividualCustomerRequest updateIndividualCustomerRequest) {
        customerBusinessRules.customerMustExists(updateIndividualCustomerRequest.getId());
        Customer customer = this.modelMapperService.forRequest().map(updateIndividualCustomerRequest, Customer.class);
        customer.setUpdatedDate(LocalDateTime.now());
        Customer updatedCustomer = customerRepository.save(customer);
        UpdatedCustomerResponse updatedCustomerResponse = this.modelMapperService.forResponse().map(updatedCustomer, UpdatedCustomerResponse.class);
        updatedCustomerResponse.setUpdatedDate(updatedCustomer.getUpdatedDate());
        return updatedCustomerResponse;
    }

    //todo: business customer ve individual customer için ayrı işlemler

    @Override
    public void delete(int id) {
        customerBusinessRules.customerMustExists(id);
        customerRepository.deleteById(id);
    }
}
