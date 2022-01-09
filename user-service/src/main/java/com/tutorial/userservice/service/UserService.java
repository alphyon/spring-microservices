package com.tutorial.userservice.service;

import com.tutorial.userservice.entity.User;
import com.tutorial.userservice.feingclients.BikeFeignClient;
import com.tutorial.userservice.feingclients.CarFeignClient;
import com.tutorial.userservice.model.Bike;
import com.tutorial.userservice.model.Car;
import com.tutorial.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CarFeignClient carFeignClient;

    @Autowired
    BikeFeignClient bikeFeignClient;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<Car> getCars(int userId) {
        return restTemplate.getForObject("http://car-service/car/byuser/" + userId, List.class);
    }

    public List<Bike> getBikes(int userId) {
        return restTemplate.getForObject("http://bike-service/bike/byuser/" + userId, List.class);
    }

    public Car saveCar(int userId, Car car) {
        car.setUserId(userId);
        return carFeignClient.save(car);
    }

    public Bike saveBike(int userId, Bike bike) {
        bike.setUserId(userId);
        return bikeFeignClient.save(bike);
    }

    public Map<String, Object> getUserAndVehicles(int userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("Message", "User not found");
            return result;
        }
        result.put("User", user);

        List<Car> cars = carFeignClient.getCars(userId);
        result.put("Cars", Objects.requireNonNullElse(cars, "User not have cars"));

        List<Bike> bikes = bikeFeignClient.getBikes(userId);
        result.put("Bikes", Objects.requireNonNullElse(bikes, "User not have bikes"));

        return result;

    }
}
