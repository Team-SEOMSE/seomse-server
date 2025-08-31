package com.seomse.user.client.service;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.seomse.IntegrationTestSupport;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientRepository;
import com.seomse.user.client.service.request.UserProfileUpdateServiceRequest;
import com.seomse.user.client.service.response.UserProfileResponse;

class ClientServiceTest extends IntegrationTestSupport {

	@Autowired
	private ClientService clientService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClientRepository clientRepository;

	@AfterEach
	void tearDown() {
		clientRepository.deleteAllInBatch();
	}

	@DisplayName("로그인 된 유저의 프로필 정보를 가져온다.")
	@Test
	void getUserProfile() {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Gender gender = Gender.MALE;
		Age age = Age.TWENTIES;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, gender,
			age);

		clientRepository.save(client);

		BDDMockito.given(securityService.getCurrentLoginUserInfo())
			.willReturn(new LoginUserInfo(client.getId(), Role.CLIENT));

		// when
		UserProfileResponse response = clientService.getUserProfile();

		// then
		Assertions.assertThat(response)
			.extracting(UserProfileResponse::email, UserProfileResponse::gender, UserProfileResponse::age)
			.contains(email, gender, age);
	}

	@DisplayName("로그인 된 유저의 프로필(성별, 나이)을 업데이트한다.")
	@Test
	@Transactional
	void updateUserProfile() {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, null,
			null);

		clientRepository.save(client);

		BDDMockito.given(securityService.getCurrentLoginUserInfo())
			.willReturn(new LoginUserInfo(client.getId(), Role.CLIENT));

		UserProfileUpdateServiceRequest request = new UserProfileUpdateServiceRequest(Gender.FEMALE, Age.TWENTIES);

		// when
		UUID result = clientService.updateUserProfile(request);

		// then
		Assertions.assertThat(result).isNotNull();

		ClientEntity foundClient = clientRepository.getReferenceById(result);
		Assertions.assertThat(foundClient)
			.extracting(ClientEntity::getGender, ClientEntity::getAge)
			.contains(request.gender(), request.age());
	}
}