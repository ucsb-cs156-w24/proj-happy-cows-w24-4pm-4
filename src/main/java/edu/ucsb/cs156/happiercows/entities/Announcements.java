package edu.ucsb.cs156.happiercows.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "announcements")
public class Announcements {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private long commonsId;
  private LocalDateTime start;
  private LocalDateTime end;
  private String announcement;
}
