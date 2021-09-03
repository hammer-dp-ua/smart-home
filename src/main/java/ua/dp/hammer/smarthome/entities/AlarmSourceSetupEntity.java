package ua.dp.hammer.smarthome.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "alarm_sources_setup")
public class AlarmSourceSetupEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="alarm_sources_setup_aa_id_seq",
         sequenceName="alarm_sources_setup_aa_id_seq",
         allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="alarm_sources_setup_aa_id_seq")
   private Integer id;

   @Column(name = "source")
   private String source;

   public Integer getId() {
      return id;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }
}
