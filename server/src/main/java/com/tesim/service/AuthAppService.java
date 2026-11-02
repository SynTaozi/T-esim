package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminAccountUpdateRequest;
import com.tesim.dto.admin.AdminSessionVO;
import com.tesim.dto.auth.AuthTokenVO;
import com.tesim.dto.auth.ChangePasswordRequest;
import com.tesim.dto.auth.LoginRequest;
import com.tesim.dto.auth.MemberVO;
import com.tesim.dto.auth.RegisterRequest;
import com.tesim.dto.auth.ResetPasswordRequest;
import com.tesim.dto.auth.SendCodeVO;
import com.tesim.dto.auth.UpdateProfileRequest;
import com.tesim.entity.Member;
import com.tesim.security.JwtUtil;
import com.tesim.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface AuthAppService {

    SendCodeVO sendRegisterCode(String email);



    SendCodeVO sendResetCode(String email);



    AuthTokenVO register(RegisterRequest req);



    AuthTokenVO login(LoginRequest req);



    AdminSessionVO adminLogin(LoginRequest req);



    AdminSessionVO adminMe();



    AdminSessionVO updateAdminAccount(AdminAccountUpdateRequest req);



    void resetPassword(ResetPasswordRequest req);



    MemberVO profile();



    MemberVO updateProfile(UpdateProfileRequest req);



    void changePassword(ChangePasswordRequest req);


}
