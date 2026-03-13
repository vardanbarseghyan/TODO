package com.vardan.todo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    @Size(min = 8)//esi kara lini kara chlni, ete lini, kopit asac attackerin khsuhes vor qo sistemy ashxatuma nenc vor passwordy pti 8hatic avel simvol lini, vor chlini el gorcy sharunakvelua hasni authenticationManager.authenticate()-in, tenc db-in dimi  tena vor tenc passwordov user chka nor error qci.
    private String password;
}
