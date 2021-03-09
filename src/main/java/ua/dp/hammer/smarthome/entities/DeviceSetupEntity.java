package ua.dp.hammer.smarthome.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device_type_names")
public class DeviceSetupEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="device_type_names_aa_id_seq",
                      sequenceName="device_type_names_aa_id_seq",
                      allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="device_type_names_aa_id_seq")
   private Integer id;

   @NotNull
   @Column(name = "device_name")
   private String name;

   @NotNull
   @ManyToOne
   @JoinColumn(name = "device_type", nullable = false)
   private DeviceTypeEntity type;

   @Column(name = "ip4_address")
   private String ip4Address;

   @OneToOne(mappedBy = "typeName", fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = true)
   @OnDelete(action = OnDeleteAction.CASCADE) // Used for DDL generations and mostly just declarative
   private ShutterActionTimeSetupEntity shutterActionTimeSetup;

   @OneToOne(mappedBy = "typeName", fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = true)
   @OnDelete(action = OnDeleteAction.CASCADE) // Used for DDL generations and mostly just declarative
   private FanSetupEntity fanSetupEntity;

   public Integer getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public DeviceTypeEntity getType() {
      return type;
   }

   public void setType(DeviceTypeEntity type) {
      this.type = type;
   }

   public String getIp4Address() {
      return ip4Address;
   }

   public void setIp4Address(String ip4Address) {
      this.ip4Address = ip4Address;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public ShutterActionTimeSetupEntity getShutterActionTimeSetup() {
      return shutterActionTimeSetup;
   }

   public void setShutterActionTimeSetup(ShutterActionTimeSetupEntity shutterActionTimeSetup) {
      this.shutterActionTimeSetup = shutterActionTimeSetup;
   }

   public FanSetupEntity getFanSetupEntity() {
      return fanSetupEntity;
   }

   public void setFanSetupEntity(FanSetupEntity fanSetupEntity) {
      this.fanSetupEntity = fanSetupEntity;
   }
}
