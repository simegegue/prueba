package domain;

import java.util.Date;
import java.util.HashMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@XmlRootElement(name = "census")
@Access(AccessType.PROPERTY)
public class Census extends DomainEntity {

	// Username del usuario que crea la votación
	private String username;
	
	// Id que identifica de forma únia a la votación
	private int idVotacion;
	
	// Título de la votación
	private String tituloVotacion;
	
	// Indica si el censo es "abierto" o "cerrado"
	private String tipoCenso;
	
	// Mapa encargado de asignar un true o false (ha votado o no) a un token
	// único de un usuario
	private HashMap<String, Boolean> votoPorUsuario = new HashMap<String, Boolean>();
	
	// Fecha en la que se inicia la votacion
	private Date fechaInicioVotacion;
	
	// Fecha en la que finaliza la votación
	private Date fechaFinVotacion;

	public Census() {
		
	}
	
	

	@MapKeyColumn(name = "token")
	@Column(name = "valor")
	@CollectionTable(name = "value", joinColumns = @JoinColumn(name = "token") )
	public HashMap<String, Boolean> getVotoPorUsuario() {
		return votoPorUsuario;
	}
	

	public void setVotoPorUsuario(HashMap<String, Boolean> votoPorUsuario) {
		this.votoPorUsuario = votoPorUsuario;
	}
	

	@NotBlank
	public String getUsername() {
		return username;
	}
	

	public void setUsername(String username) {
		this.username = username;
	}
	

	@Column(unique = true)
	public int getIdVotacion() {
		return idVotacion;
	}
	

	public void setIdVotacion(int idVotacion) {
		this.idVotacion = idVotacion;
	}
	

	@NotBlank
	public String getTituloVotacion() {
		return tituloVotacion;
	}
	

	public void setTituloVotacion(String tituloVotacion) {
		this.tituloVotacion = tituloVotacion;
	}
	

	@NotBlank
	public String getTipoCenso() {
		return tipoCenso;
	}

	public void setTipoCenso(String tipoCenso) {
		this.tipoCenso = tipoCenso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
	@NotNull
	public Date getFechaInicioVotacion() {
		return fechaInicioVotacion;
	}
	

	public void setFechaInicioVotacion(Date fechaInicioVotacion) {
		this.fechaInicioVotacion = fechaInicioVotacion;
	}
	

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
	@NotNull
	public Date getFechaFinVotacion() {
		return fechaFinVotacion;
	}

	public void setFechaFinVotacion(Date fechaFinVotacion) {
		this.fechaFinVotacion = fechaFinVotacion;
	}

}
