package com.tutorial.userservice.controller;

import com.tutorial.userservice.entity.User;
import com.tutorial.userservice.model.Bike;
import com.tutorial.userservice.model.Car;
import com.tutorial.userservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping()
    public ResponseEntity<User> save(@RequestBody User user) {
        User userNew = userService.save(user);
        return ResponseEntity.ok(userNew);
    }

    @CircuitBreaker(name = "carsCB", fallbackMethod = "fallBackCars")
    @GetMapping("/cars/{userId}")
    public ResponseEntity<List<Car>> getCars(@PathVariable("userId") int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Car> cars = userService.getCars(userId);

        return ResponseEntity.ok(cars);
    }

    @CircuitBreaker(name = "carsCB", fallbackMethod = "fallBackCarSave")
    @PostMapping("/savecar/{userId}")
    public ResponseEntity<Car> saveCar(@PathVariable("userId") int userId, @RequestBody Car car) {
        if (userService.getUserById(userId) == null) {
            return ResponseEntity.notFound().build();
        }
        Car carNew = userService.saveCar(userId, car);
        return ResponseEntity.ok(carNew);
    }

    @CircuitBreaker(name = "bikesCB", fallbackMethod = "fallBackBikes")
    @GetMapping("/bikes/{userId}")
    public ResponseEntity<List<Bike>> getBikes(@PathVariable("userId") int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Bike> cars = userService.getBikes(userId);

        return ResponseEntity.ok(cars);
    }

    @CircuitBreaker(name = "bikesCB", fallbackMethod = "fallBackBikesSave")
    @PostMapping("/savebike/{userId}")
    public ResponseEntity<Bike> saveCar(@PathVariable("userId") int userId, @RequestBody Bike bike) {
        if (userService.getUserById(userId) == null) {
            return ResponseEntity.notFound().build();
        }
        Bike bikeNew = userService.saveBike(userId, bike);
        return ResponseEntity.ok(bikeNew);
    }

    @CircuitBreaker(name="allCB", fallbackMethod = "fallBackGetAll")
    @GetMapping("/getfulldata/{userId}")
    public ResponseEntity<Map<String, Object>> getAllVehicles(@PathVariable("userId") int userId) {
        Map<String, Object> result = userService.getUserAndVehicles(userId);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<List<Car>> fallBackCars(@PathVariable("userId") int userId, RuntimeException e){
        return new ResponseEntity("User "+userId+" cars are in service", HttpStatus.OK);
    }

    private ResponseEntity<Car> fallBackCarSave(@PathVariable("userId") int userId,@RequestBody Car car, RuntimeException e){
        return new ResponseEntity("User "+userId+" not response cars", HttpStatus.OK);
    }

    private ResponseEntity<List<Bike>> fallBackBikes(@PathVariable("userId") int userId, RuntimeException e){
        return new ResponseEntity("User "+userId+" bikes are in service", HttpStatus.OK);
    }

    private ResponseEntity<Bike> fallBackBikesSave(@PathVariable("userId") int userId,@RequestBody Bike bike, RuntimeException e){
        return new ResponseEntity("User "+userId+" not response bikes", HttpStatus.OK);
    }


    public ResponseEntity<Map<String, Object>> fallBackGetAll(@PathVariable("userId") int userId,RuntimeException e) {
        return new ResponseEntity("User "+userId+" vehicles are in service", HttpStatus.OK);

    }
}
