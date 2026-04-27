package org.canopyplatform.canopy.downloadservice.entities;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "view_study")
@NoArgsConstructor
@AllArgsConstructor
public class ViewStudy {
	@Id
	@Column(name = "study_id")
	private Integer studyId;

	@Column(name = "title")
	private String studyName;

	@Column(name = "status")
	private String submissionStatus;

	@Column(name = "created_at")
	private Timestamp createdAt;

	@Column(name = "center")
	private String center;

}
