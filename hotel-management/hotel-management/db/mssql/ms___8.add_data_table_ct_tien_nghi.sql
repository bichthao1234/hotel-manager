USE hotel_management;
GO

INSERT INTO CT_TIEN_NGHI (ID_TN, ID_HANG_PHONG, MO_TA, SO_LUONG)
SELECT TN.ID_TN, HP.ID_HANG_PHONG, '', 1
FROM TIEN_NGHI TN
CROSS JOIN HANG_PHONG HP;