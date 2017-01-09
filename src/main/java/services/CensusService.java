package services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import repositories.CensusRepository;
import utilities.Gmail;
import utilities.RESTClient;
import domain.Census;

@Service
@Transactional
public class CensusService {

	// Atributo necesario para mandar email con la modificación del censo
	private static String cuerpoEmail = "";

	// Managed repository -----------------------------------------------------

	@Autowired
	private CensusRepository censusRepository;

	// Constructors -----------------------------------------------------------

	public CensusService() {
		super();
	}

	// Methods ----------------------------------------------------------------
	
	public Collection<Census> findAllCensus(){
		Collection<Census> result;
		result = censusRepository.allCensus();
		Assert.notNull(result);
		return result;
	}

	/**
	 * Crea un censo a partir de una votación
	 * 
	 * @param idVotacion
	 *            = Identificador de la votación
	 * @param username
	 *            = Nombre de usuario que ha creado la votacion
	 * @param fechaInicio
	 *            = Fecha de inicio de la votacion
	 * @param fechaFin
	 *            = Fecha de fin de la votacion
	 * @param tituloVotacion
	 *            Cadena de texto con el titulo de la votacion
	 * @param tipoVotacion
	 *            Cadena de texto con el tipo de la votacion (abierta o cerrada)
	 * @return census
	 * @throws ParseException
	 */

	public Census create(int idVotacion, String username, String fechaInicio, String fechaFin, String tituloVotacion,
			String tipoVotacion) throws ParseException {
		Assert.isTrue(!username.equals(""));
		Assert.isTrue(tipoVotacion.equals("abierta") || tipoVotacion.equals("cerrada"));
		Census result = new Census();
		long startDate = Long.parseLong(fechaInicio);
		long finishDate = Long.parseLong(fechaFin);

		Date fechaComienzo = new Date(startDate);
		Date fechaFinal = new Date(finishDate);
		Assert.isTrue(fechaComienzo.before(fechaFinal));

		result.setFechaFinVotacion(fechaFinal);
		result.setFechaInicioVotacion(fechaComienzo);

		result.setIdVotacion(idVotacion);
		result.setTituloVotacion(tituloVotacion);
		if (tipoVotacion.equals("abierta")) {
			result.setTipoCenso("abierto");
		} else {
			result.setTipoCenso("cerrado");
		}
		result.setUsername(username);
		HashMap<String, Boolean> vpo = new HashMap<String, Boolean>();
		result.setVotoPorUsuario(vpo);
		return result;
	}

	/**
	 * Metodo que devuelve los censos en los que se puede registrar un usuario.
	 * Para ello, el censo tiene que ser abierto, estar la votacion activa y que
	 * el usuario dado no se encuentre ya registrado
	 * 
	 * @param username
	 */

	public Collection<Census> findCensusesToRegisterByUser(String username) {
		Assert.hasLength(username);
		Collection<Census> result = new ArrayList<Census>();
		Collection<Census> openedCensuses = new ArrayList<Census>();
		openedCensuses = censusRepository.findAllOpenedCensuses();

		for (Census c : openedCensuses) {
			if (!c.getVotoPorUsuario().containsKey(username)
					&& votacionActiva(c.getFechaInicioVotacion(), c.getFechaFinVotacion())) {
				result.add(c);
			}
		}
		return result;
	}

	public Collection<Census> findAll(int censusID) {
		Collection<Census> result;
		result = censusRepository.findAll();
		Assert.notNull(result);
		return result;
	}

	/**
	 * Método usado por cabina que actualiza a true el estado de voto de un user
	 * 
	 * @param idVotacion
	 *            = Identificador de la votación
	 * @param username
	 *            = Nombre de usuario
	 * @return boolean
	 */

	public boolean updateUser(int idVotacion, String tipoVotacion, String username) {
		boolean result = false;
		Assert.isTrue(!username.equals(""));
		Census c = findCensusByVote(idVotacion);
		HashMap<String, Boolean> vpo = c.getVotoPorUsuario();

		if (vpo.containsKey(username) && !vpo.get(username)) {
			vpo.remove(username);
			vpo.put(username, true);
			result = true;
		}

		c.setVotoPorUsuario(vpo);
		save(c);

		return result;

	}

	/**
	 * Devuelve un json para saber si se puede borrar o no una votación
	 * 
	 * @param idVotacion
	 *            = Identificador de la votación
	 * @param username
	 *            = Nombre de usuario
	 * @return format json
	 */

	public String canDelete(int idVotacion, String username) {
		Assert.hasLength(username);
		String res = "";
		Census c = findCensusByVote(idVotacion);

		if (c.getVotoPorUsuario().isEmpty()) {

			// Si se puede se elimina

			res = "[{\"result\":\"yes\"}]";
			delete(c.getId(), username);
		} else {
			res = "[{\"result\":\"no\"}]";
		}
		return res;
	}

	/**
	 * Devuelve un json indicando si un usuario puede votar en una determinada
	 * votación
	 * 
	 * @param idVotacion
	 *            = Identificador de la votación
	 * @param username
	 *            = Nombre de usuario
	 * @return string format json
	 */

	public String canVote(int idVotacion, String username) {
		Assert.isTrue(!username.equals(""));
		String result = "";
		Boolean canVote = false;

		Census census = findCensusByVote(idVotacion);

		if (census.getVotoPorUsuario().containsKey(username) && !census.getVotoPorUsuario().get(username)) {
			canVote = true;

		}

		if (canVote) {
			result = "{\"result\":\"yes\"}";
		} else {
			result = "{\"result\":\"no\"}";
		}

		return result;
	}

	/**
	 * Método que devuelve todos los censos de las votaciones en las que un
	 * usuario puede votar.
	 * 
	 * @param username
	 *            = Nombre de usuario
	 * @return Collection<census>
	 */

	public Collection<Census> findPossibleCensusesByUser(String username) {
		Assert.isTrue(username != "");
		Collection<Census> allCensuses = findAll();
		Collection<Census> result = new ArrayList<Census>();

		for (Census c : allCensuses) {

			// Comprobamos si la votación está activa

			if (votacionActiva(c.getFechaInicioVotacion(), c.getFechaFinVotacion())) {
				if (c.getVotoPorUsuario().containsKey(username) && !c.getVotoPorUsuario().get(username)) {
					result.add(c);
				}
			}
		}
		return result;
	}

	/**
	 * Devuelve todos los censos que de un propietario
	 * 
	 * @param username
	 *            = Nombre de usuario
	 * @return Collection<census>
	 */

	public Collection<Census> findCensusByCreator(String username) {
		Assert.hasLength(username);
		Collection<Census> result = censusRepository.findCensusByCreator(username);
		return result;
	}

	/**
	 * Devuelve un determinado censo de un propietario
	 * 
	 * @param censusId
	 * @param username
	 * @return Census
	 */

	public Census findOneByCreator(int censusId, String username) {
		Assert.isTrue(!username.equals(""));
		Census result;
		result = findOne(censusId);
		Assert.isTrue(result != null);
		Assert.isTrue(result.getUsername().equals(username));

		return result;
	}

	/**
	 * 
	 * Añade un usuario con un username determidado a un censo CERRADO
	 *
	 * @param censusId
	 *            = Identificador del censo al que añadir el usuario
	 * @param username
	 *            = Creador (propietario) del censo
	 * @param username_add
	 *            = Usuario que se va a añadir al censo
	 */

	public void addUserToClosedCensus(int censusId, String username, String usernameAdd) {
		Census census = findOne(censusId);
		Assert.isTrue(census.getTipoCenso().equals("cerrado"));
		Assert.isTrue(votacionActiva(census.getFechaInicioVotacion(), census.getFechaFinVotacion()));
		Assert.isTrue(census.getUsername().equals(username));
		HashMap<String, Boolean> vpo = census.getVotoPorUsuario();

		Assert.isTrue(!vpo.containsKey(usernameAdd));
		vpo.put(usernameAdd, false);
		census.setVotoPorUsuario(vpo);
		save(census);

		// Envío de correo

		String dirEmail;

		// Fecha para controlar cuándo se produce un cambio en el censo

		Date currentMoment = new Date();
		Map<String, String> usernamesAndEmails = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		dirEmail = usernamesAndEmails.get(usernameAdd);
		cuerpoEmail = currentMoment.toString() + "-> Se ha incorporado al censo de " + census.getTituloVotacion();
		try {

			// Se procede al envío del correo con el resultado de la inclusión
			// en el censo
			Gmail.send(cuerpoEmail, dirEmail);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Añade un usuario a un censo ABIERTO (registrarse en un censo abierto)
	 *
	 * @param censusId
	 *            Identificador del censo al que añadir el usuario
	 * @param username_add
	 *            Nombre de usuario que se va a añadir al censo
	 */

	public void addUserToOpenedCensus(int censusId, String usernameAdd) {
		Census census = findOne(censusId);
		Assert.isTrue(census.getTipoCenso().equals("abierto"));
		Assert.isTrue(votacionActiva(census.getFechaInicioVotacion(), census.getFechaFinVotacion()));
		HashMap<String, Boolean> vpo = census.getVotoPorUsuario();
		Assert.isTrue(!vpo.containsKey(usernameAdd));
		vpo.put(usernameAdd, false);
		census.setVotoPorUsuario(vpo);
		save(census);

		// Envío de correo

		String dirEmail;
		// Fecha para controlar cuando se
		// produce un cambio en el censo

		Date currentMoment = new Date();

		Map<String, String> usernamesAndEmails = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		dirEmail = usernamesAndEmails.get(usernameAdd);
		cuerpoEmail = currentMoment.toString() + "-> Se ha incorporado al censo de " + census.getTituloVotacion();
		try {

			// Se procede al envio del correo con el resultado de la inclusión
			// en el censo
			Gmail.send(cuerpoEmail, dirEmail);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Elimina un usuario con un username determidado de un censo CERRADO,
	 * cumpliendo la condicion de que el usuario no tenga voto en ese censo
	 *
	 * @param censusId
	 *            = Identificador del censo
	 * @param username
	 *            = Creador (propietario) del censo
	 * @param username_add
	 *            = Usuario que se va a eliminar del censo
	 */
	public void removeUserOfClosedCensus(int censusId, String username, String username_remove) {
		Census census = findOne(censusId);
		Assert.isTrue(census.getTipoCenso().equals("cerrado"));
		Assert.isTrue(votacionActiva(census.getFechaInicioVotacion(), census.getFechaFinVotacion()));
		HashMap<String, Boolean> vpo = census.getVotoPorUsuario();
		Assert.isTrue(census.getUsername().equals(username));

		Assert.isTrue(vpo.containsKey(username_remove) && !vpo.get(username_remove));
		vpo.remove(username_remove);
		census.setVotoPorUsuario(vpo);
		save(census);

		// Envío de correo

		String dirEmail;

		// Fecha para controlar cuando se produce un cambio en el censo

		Date currentMoment = new Date();

		Map<String, String> usernamesAndEmails = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		dirEmail = usernamesAndEmails.get(username_remove);
		cuerpoEmail = currentMoment.toString() + "-> Se ha eliminado del censo de " + census.getTituloVotacion();

		// Se procede al envio del correo con el resultado de la exclusión del
		// usuario del censo
		try {
			Gmail.send(cuerpoEmail, dirEmail);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Guardar un censo
	 * 
	 * @param census
	 * @return census
	 */

	public Census save(Census census) {
		Census c = censusRepository.save(census);
		return c;
	}

	/**
	 * Elimina un censo si no tiene usuarios
	 * 
	 * @param censusId
	 *            = Identificador del censo
	 * @param username
	 */

	public void delete(int censusId, String username) {
		Census c = findOne(censusId);

		// Puedo borrarlo siempre y cuando no haya usuarios registrados

		Assert.isTrue(c.getVotoPorUsuario().isEmpty());
		Assert.isTrue(c.getUsername().equals(username));
		censusRepository.delete(censusId);
	}

	/**
	 * Encuentra un censo dado su id
	 * 
	 * @param censusId
	 *            = Identificador del censo
	 * @return census
	 */

	public Census findOne(int censusId) {
		Census c = censusRepository.findOne(censusId);
		Assert.notNull(c);
		return c;
	}

	/**
	 * Metodo que devuelve un json informando sobre un determinado usuario y su
	 * estado en el voto
	 * 
	 * @param idVotacion
	 *            = Identificador de la votacion
	 * @param username
	 *            = Usuario del cual queremos obtener su estado de voto
	 * @return String
	 */

	public String createResponseJson(int idVotacion, String username) {
		String response = "";
		Census c = findCensusByVote(idVotacion);

		if (c.getVotoPorUsuario().get(username)) {
			response = response + "{\"idVotacion\":" + idVotacion + ",\"username\":\"" + username + "\",\"result\":"
					+ c.getVotoPorUsuario().get(username) + "}";
		} else {
			response = response + "{\"result\":" + "0}";
		}
		return response;
	}

	/**
	 * Encuentra todos los censos del sistema
	 * 
	 * @return Collection<Census>
	 */

	public Collection<Census> findAll() {
		Collection<Census> result;
		result = censusRepository.findAll();
		return result;
	}

	/**
	 * Metodo para buscar el censo de una votación
	 * 
	 * @param idVotacion
	 *            = Id de la votacion sobre la que se busca un censo
	 * @return census
	 */

	public Census findCensusByVote(int idVotacion) {
		Census result = censusRepository.findCensusByVote(idVotacion);
		Assert.notNull(result);
		return result;
	}

	/**
	 *
	 * Método creado para saber si existe una votacion activa en el rango de
	 * fechas. Una votacion sera activa si su fecha de fin es posterior a la
	 * fecha actual.
	 * 
	 * @param fechaInicio
	 *            = Fecha inicio de la votacion
	 * @param fechaFin
	 *            = Fecha fin de la votacion
	 * @return true si está activa
	 */

	private boolean votacionActiva(Date fechaInicio, Date fechaFin) {
		Boolean res = false;
		Date fechaActual = new Date();
		Long fechaActualLong = fechaActual.getTime();
		Long fechaFinLong = fechaFin.getTime();
		if (fechaFinLong > fechaActualLong) {
			res = true;
		}
		return res;
	}

	// NUEVA FUNCIONALIDAD 2015/2016

	/**
	 * Método para filtrar usuarios de un censo
	 * 
	 * @param username
	 *            = Username del usuario que buscamos
	 * @param censusId
	 *            = Id del censo sobre el que vamos a realizar la búsqueda
	 * @return el filtro de búsqueda
	 */

	public Collection<String> findByUsername(String username, int censusId) {
		Assert.hasLength(username);
		Assert.isTrue(censusId > 0);
		Census censo = findOne(censusId);
		Assert.notNull(censo);
		Collection<String> result = new ArrayList<String>();
		Map<String, String> map = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		Collection<String> usernames = map.keySet();

		for (String user : usernames) {
			if (user.contains(username)) {

				// Añadimos al resultado los votantes que pasan el filtro

				result.add(user);
			}
		}
		return result;
	}
	
	// NUEVA FUNCIONALIDAD 2016/2017

		/**
		* Método para obtener los censo que han finalizado recientemente.				 * 
		* @param username
	    *            = Username del usuario que buscamos
		*/
		public Collection<Census> findRecentFinishedCensus(String user){
			Collection<Census> aux;
			Collection<Census> finished=new HashSet<Census>();
			aux=censusRepository.findAllCensusFinishedRecently();
			for(Census c:aux){
				if(c.getVotoPorUsuario().containsKey(user)){
					finished.add(c);
				}
			}
			return finished;
		}

}
