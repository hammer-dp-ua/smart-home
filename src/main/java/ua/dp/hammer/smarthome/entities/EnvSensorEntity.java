package ua.dp.hammer.smarthome.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "env_sensors_data")
public class EnvSensorEntity {
   @Id
   @Column(name = "aa_id")
   @GenericGenerator(name = "EnvSensorEntityIdGenerator", strategy = "increment")
   private Integer id;

   @NotNull
   @OneToOne(optional = false)
   @PrimaryKeyJoinColumn
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
