package com.seomse.fixture.interaction.appointment;

import com.seomse.interaction.appointment.entity.AppointmentDetailEntity;
import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;

public class AppointmentDetailFixture {
	public static AppointmentDetailEntity createAppointmentDetailEntity(AppointmentEntity appointment) {
		return new AppointmentDetailEntity(appointment,
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"requirements",
			"/image.png"
		);
	}
}
