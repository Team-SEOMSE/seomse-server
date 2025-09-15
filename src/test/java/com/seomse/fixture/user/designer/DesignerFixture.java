package com.seomse.fixture.user.designer;

import com.seomse.user.designer.entity.DesignerEntity;

public class DesignerFixture {
	public static DesignerEntity createDesignerEntity() {
		return new DesignerEntity("designer@email.com", "designer1234!", "designerNickName");
	}
}
