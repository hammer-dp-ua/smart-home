package ua.dp.hammer.smarthome.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "env_sensors_data")
public class EnvSensorEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   //@GeneratedValue(strategy = GenerationType.IDENTITY)
   /*@GenericGenerator(name = "EnvSensorEntitySequence",
         strategy = "enhanced-sequence",
         parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "env_sensors_data_aa_id_seq")})*/
   @SequenceGenerator(name="env_sensors_data_aa_id_seq",
                      sequenceName="env_sensors_data_aa_id_seq",
                      allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="env_sensors_data_aa_id_seq")
   private Integer id;

   @NotNull
   @OneToOne(optional = false)
   @JoinColumn(name = "technical_device_info")
   private TechnicalDeviceInfoEntity technicalInfo;

   @Column(name = "temperature")
   private Float temperature;

   @Column(name = "humidity")
   private Float humidity;

   @Column(name = "light")
   private Short light;

   public Integer getId() {
      return id;
   }

   public TechnicalDeviceInfoEntity getTechnicalInfo() {
      return technicalInfo;
   }

   public void setTechnicalInfo(TechnicalDeviceInfoEntity technicalInfo) {
      this.technicalInfo = technicalInfo;
   }

   public Float getTemperature() {
      return temperature;
   }

   public void setTemperature(Float temperature) {
      this.temperature = temperature;
   }

   public Float getHumidity() {
      return humidity;
   }

   public void setHumidity(Float humidity) {
      this.humidity = humidity;
   }

   public Short getLight() {
      return light;
   }

   public void setLight(Short light) {
      this.light = light;
   }
}
