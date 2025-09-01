package com.jio.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RemotePiiScanRequestDto extends PiiScanRequestDto {
	private SshConnectionDto connection;
}
