package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.ClientRole;
import com.krishu.caretracev2.CustomExceptions.BadCredentialsException;
import com.krishu.caretracev2.CustomExceptions.UserAlreadyExistsException;
import com.krishu.caretracev2.DTO.LoginRequest;
import com.krishu.caretracev2.DTO.RegisterRequest;
import com.krishu.caretracev2.DTO.RegisterResponse;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.Client;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.UserRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final CareTakerRepo careTakerRepo;

    public AuthService(UserRepo userRepo, BCryptPasswordEncoder encoder, JwtService jwtService, CareTakerRepo careTakerRepo) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.careTakerRepo = careTakerRepo;
    }

    public RegisterResponse registerUser(RegisterRequest request){
        if(userRepo.findByEmail(request.getEmail()).isPresent()){
            throw new UserAlreadyExistsException("User is Already exists with this email");
        }
        Client client=new Client();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPassword(encoder.encode(request.getPassword()));
        client.setRole(ClientRole.CARETAKER);

        Client savedClient=userRepo.save(client);
        CareTaker careTaker=new CareTaker();
        careTaker.setUserId(savedClient.getId());
        careTakerRepo.save(careTaker);
        return mapToUserResponse(savedClient);
    }

    public String loginUser(LoginRequest request) {
        Client user=userRepo.findByEmail(request.getEmail()).orElseThrow(()->new BadCredentialsException("Bad Credentials"));
        if(!encoder.matches(request.getPassword(),user.getPassword())){
            throw new BadCredentialsException("Bad Credentials");
        }
        return jwtService.generateToken(user);
    }

    private RegisterResponse mapToUserResponse(Client client){
        RegisterResponse response=new RegisterResponse();
        response.setId(client.getId());
        response.setName(client.getName());
        response.setEmail(client.getEmail());
        return response;
    }
}
