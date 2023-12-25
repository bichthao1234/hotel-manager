USE hotel_management;

-- add DON_VI_TINH to DICH_VU
ALTER TABLE dbo.DICH_VU
ADD DON_VI_TINH VARCHAR(255);

-- alter column CMND in PHIEU_DAT
ALTER TABLE PHIEU_DAT
ALTER COLUMN CMND NVARCHAR(20) NULL;

-- Change table HOA_DON
CREATE TABLE HOA_DON_New
(
    ID_HD BIGINT IDENTITY(1, 1) PRIMARY KEY,
    NGAY_LAP DATE NOT NULL,
    ID_NV VARCHAR(20),
    ID_PT BIGINT,
    FOREIGN KEY (ID_NV) REFERENCES NHAN_VIEN(ID_NV),
    FOREIGN KEY (ID_PT) REFERENCES PHIEU_THUE(ID_PT)
);

INSERT INTO HOA_DON_New (NGAY_LAP, ID_NV, ID_PT)
SELECT NGAY_LAP, ID_NV, MAPT
FROM HOA_DON;

EXEC sp_rename 'HOA_DON', 'HOA_DON_Old';
EXEC sp_rename 'HOA_DON_New', 'HOA_DON';

ALTER TABLE CT_PHIEU_THUE
DROP CONSTRAINT FK__CT_PHIEU___ID_HD__52593CB8;

alter table CT_PHIEU_THUE
alter column ID_HD bigint null
ALTER TABLE CT_PHIEU_THUE
ADD CONSTRAINT FK__CT_PHIEU___ID_HD
FOREIGN KEY (ID_HD) REFERENCES HOA_DON(ID_HD);

DROP TABLE HOA_DON_Old;
