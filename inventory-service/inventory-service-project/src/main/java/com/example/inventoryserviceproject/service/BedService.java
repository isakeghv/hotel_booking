package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewNewBedType;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.model.Bed;
import com.example.inventoryserviceproject.repository.BedRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service containing the business logic for bed types.
 */
@Service
public class BedService {
    private final BedRepository bedRepository;

    /**
     * Creates the service with the repository it uses.
     *
     * @param bedRepository repository for bed types
     */
    public BedService(BedRepository bedRepository){
        this.bedRepository = bedRepository;
    }

    /**
     * Creates and stores a new bed type.
     *
     * @param request the validated details of the new bed type
     * @return the stored bed type
     * @throws InternalServerException if the bed type could not be saved
     */
    public Bed createNewBed(ReviewNewBedType request){
        try{
            Bed bed = new Bed(request);
            bedRepository.save(bed);
            return bed;
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to save new bed-type.");
        }
    }

    /**
     * Retrieves all bed types. Returns an empty list rather than failing when
     * none exist.
     *
     * @return the list of bed types, possibly empty
     */
    public List<Bed> getAllBeds(){
        // Always return List, even if empty -> not having items should not throw err for user
        return bedRepository.findAll();
    }

    /**
     * Retrieves a single bed type by its id.
     *
     * @param bedId the id of the bed type
     * @return the bed type
     * @throws NotFoundException if no bed type has the given id
     */
    public Bed getSingleBed(UUID bedId){
        Optional<Bed> bed = bedRepository.findByBedId(bedId);

        if (bed.isEmpty()) throw new NotFoundException("Bed not found");

        return bed.get();
    }
}
