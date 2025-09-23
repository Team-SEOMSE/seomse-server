package com.seomse.fixture.interaction.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.user.client.entity.ClientEntity;

public class AppointmentFixture {
	public static AppointmentEntity createAppointmentEntity(ClientEntity client, DesignerShopEntity designerShop) {
		LocalDate testDate = LocalDate.now().plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);
		return new AppointmentEntity(client, designerShop,
			testDate, testTime, "serviceName");
	}
}
