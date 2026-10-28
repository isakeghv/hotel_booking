package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewNewExtra;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.repository.ExtraRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service containing the business logic for extras.
 */
@Service
public class ExtrasService {
    private final ExtraRepository extraRepository;

    /**
     * Creates the service with the repository it uses.
     *
     * @param extraRepository repository for extras
     */
    public ExtrasService(ExtraRepository extraRepository){
        this.extraRepository = extraRepository;
    }

    /**
     * Creates and stores a new extra.
     *
     * @param request the validated details of the new extra
     * @return the stored extra
     * @throws InternalServerException if the extra could not be saved
     */
    public Extra createNewExtra(ReviewNewExtra request){
        try{
            Extra extra = new Extra(request);
            extraRepository.save(extra);
            return extra;
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to save new bed-type.");
        }
    }

    /**
     * Retrieves all extras. Returns an empty list rather than failing when
     * none exist.
     *
     * @return the list of extras, possibly empty
     */
    public List<Extra> getAllExtras(){
        // Always return List, even if empty -> not having items should not throw err for user
        return extraRepository.findAll();
    }

    /**
     * Retrieves a single extra by its id.
     *
     * @param extraId the id of the extra
     * @return the extra
     * @throws NotFoundException if no extra has the given id
     */
    public Extra getExtraById(UUID extraId){
        Optional<Extra> extra = extraRepository.findById(extraId);
        if (extra.isEmpty()) throw new NotFoundException("Item not found");
        return extra.get();
    }
}
