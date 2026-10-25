package Aerosaga.withsky.AerosagaProject.service;

import Aerosaga.withsky.AerosagaProject.entity.Drone;
import Aerosaga.withsky.AerosagaProject.repositry.DroneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DroneService {
    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    public Drone register(Drone drone){
        if (droneRepository.findByName(drone.getName()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Drone Name already Exists"
            );
        }
        drone.setBatteryLevel(100.0);
        drone.setStatus("IDLE");
        drone.setGpsStatus("Connected");
        return droneRepository.save(drone);
    }
    public List<Drone> getAll(){
        return droneRepository.findAll();
    }
    public Drone getById(Long id){
        return droneRepository.findById(id).orElseThrow(()->new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Drone not found : "+id
        ));
    }
    public Drone updateBattery(Long id, double batteryLevel){
        if(batteryLevel<10|| batteryLevel>100){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Battery must be 0 to 100"
            );
        }
        Drone drone=getById(id);
        drone.setBatteryLevel(batteryLevel);
        if (batteryLevel <= 10) {
            drone.setStatus("LOW_BATTERY");
        } else if ("LOW_BATTERY".equals(drone.getStatus())) {
            drone.setStatus("IDLE");
        }
        return droneRepository.save(drone);
    }


}
