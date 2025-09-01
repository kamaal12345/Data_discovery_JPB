package com.jio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StatsDto {
	private long totalUsers;
	private long activeUsers;
	private long reportCount;
	private long scanCount;

}
