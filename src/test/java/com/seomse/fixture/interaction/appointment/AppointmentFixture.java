package com.seomse.fixture.interaction.appointment;

import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.user.client.entity.ClientEntity;

public class AppointmentFixture {
	public static AppointmentEntity createAppointmentEntity(ClientEntity client, DesignerShopEntity designerShop) {
		return new AppointmentEntity(client, designerShop, "serviceName");
	}
}
