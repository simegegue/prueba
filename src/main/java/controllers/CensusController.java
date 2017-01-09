
/* CustomerController.java
 *
 * Copyright (C) 2013 Universidad de Sevilla
 * 
 * The use of this project is hereby constrained to the conditions of the 
 * TDG Licence, a copy of which you may download from 
 * http://www.tdg-seville.info/License.html
 * 
 */

package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import domain.Census;
import domain.User;
import services.CensusService;
import utilities.RESTClient;

@Controller
@RequestMapping("/census")
public class CensusController extends AbstractController {

	@Autowired
	private CensusService censusService;

	// Constructors -----------------------------------------------------------

	public CensusController() {
		super();
	}

	/****
	 * Metodos externos
	 ******/

	// Create census ----------------------------------------------------------
	// Recibe parametros de votacion y crea un censo por votación

	@RequestMapping(value = "/create", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Census create(@RequestParam int idVotacion, @RequestParam String fechaInicio,
			@RequestParam String fechaFin, @RequestParam String tituloVotacion, String tipoVotacion,
			@CookieValue("user") String username) throws ParseException {
		Census result = null;

		Census c = censusService.create(idVotacion, username, fechaInicio, fechaFin, tituloVotacion, tipoVotacion);
		try {
			result = censusService.save(c);
		} catch (Exception oops) {
			oops.getCause();
		}
		return result;
	}

	// Devuelve JSon a a votaciones para saber si pueden borrar una votación
	// En caso afirmativo, el censo se borrará automáticamente al dar una
	// respuesta positiva -----------------------------------------------------

	@RequestMapping(value = "/canDelete", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String canDelete(@RequestParam int idVotacion, @CookieValue("user") String username) {
		return censusService.canDelete(idVotacion, username);
	}

	// Devuelve JSon a cabina para saber si un usuario puede votar ------------

	@RequestMapping(value = "/canVote", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String canVote(@RequestParam int idVotacion, @CookieValue("user") String username) {
		return censusService.canVote(idVotacion, username);
	}

	// Actualiza el estado de un usuario en una votación por cabina -----------

	@RequestMapping(value = "/updateUser", method = RequestMethod.GET)
	public @ResponseBody String updateUser(@RequestParam int idVotacion, @RequestParam String tipoVotacion,
			@CookieValue("user") String username) {
		try {
			if (censusService.updateUser(idVotacion, tipoVotacion, username)) {
				return new String("{\"result\":\"yes\"}");
			} else {
				return new String("{\"result\":\"no\"}");
			}

		} catch (Exception oops) {
			return new String("{\"result\":\"no\"}");
		}

	}

	// Devuelve un censo con sus usuarios para deliberaciones al preguntar por
	// una votación ------------

	@RequestMapping(value = "/findCensusByVote", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Census findCensusByVote(@RequestParam int idVotacion) {
		return censusService.findCensusByVote(idVotacion);
	}

	/*****
	 * Métodos internos
	 ******/

	// Método para la vista de votaciones por usuario -------------------------

	@RequestMapping(value = "/votesByUser", method = RequestMethod.GET)
	public ModelAndView getVotesByUser(@CookieValue("user") String username) {
		ModelAndView result = new ModelAndView("census/votesByUser");
		Collection<Census> cs;
		cs = censusService.findPossibleCensusesByUser("admin1");
		result.addObject("misVotaciones", true);
		result.addObject("censuses", cs);
		result.addObject("requestURI", "census/votesByUser.do");

		return result;
	}

	// Metodo para la vista de censos por creador -----------------------------

	@RequestMapping(value = "/getAllCensusByCreador", method = RequestMethod.GET)
	public ModelAndView getAllCensusByCreador(@CookieValue("user") String username) {
		ModelAndView result = new ModelAndView("census/misCensos");
		Collection<Census> cs;
		cs = censusService.findCensusByCreator(username);
		result.addObject("censuses", cs);
		result.addObject("misVotaciones", false);
		result.addObject("requestURI", "census/getAllCensusByCreador.do");

		return result;
	}

	// Añadir usuarios a un censo cerrado, como administrador del censo -------

	@RequestMapping(value = "/addUser", method = RequestMethod.GET)
	public ModelAndView addUser(@RequestParam int censusId, @CookieValue("user") String username,
			@RequestParam String usernameAdd) {
		ModelAndView result = new ModelAndView("census/misVotaciones");
		try {

			censusService.addUserToClosedCensus(censusId, username, usernameAdd);
			result = new ModelAndView("redirect:/census/edit.do?censusId=" + censusId);

		} catch (Exception oops) {
			result = new ModelAndView("redirect:/census/edit.do?censusId=" + censusId);
			result.addObject("message", "error");
			JOptionPane.showMessageDialog(null, "Error");
			oops.getStackTrace();
		}

		return result;
	}

	// Registrarse en un censo abierto y activo -------------------------------

	@RequestMapping(value = "/registerUser", method = RequestMethod.GET)
	public ModelAndView addUser(@RequestParam int censusId, @CookieValue("user") String username) {
		ModelAndView result = null;
		try {

			censusService.addUserToOpenedCensus(censusId, username);
			result = new ModelAndView("redirect:/census/getCensusesToRegister.do");

		} catch (Exception oops) {
			result = new ModelAndView("redirect:/census/getCensusesToRegister.do");
			result.addObject("message", "error");
			JOptionPane.showMessageDialog(null, "Error");
			oops.getStackTrace();
		}

		return result;
	}

	// Remove Users -----------------------------------------------------------

	@RequestMapping(value = "/removeUser", method = RequestMethod.GET)
	public ModelAndView removeUser(@RequestParam int censusId, @CookieValue("user") String username,
			@RequestParam String usernameRemove) {
		ModelAndView result = null;
		try {

			censusService.removeUserOfClosedCensus(censusId, username, usernameRemove);

			result = new ModelAndView("redirect:/census/edit.do?censusId=" + censusId);

		} catch (Exception oops) {
			result = new ModelAndView("redirect:/census/edit.do?censusId=" + censusId);
			result.addObject("message", "error");
			JOptionPane.showMessageDialog(null, "Error");
			oops.getStackTrace();
		}

		return result;
	}

	// Encontrar un usuario mediante un keyword -------------------------------

	@RequestMapping(value = "/searchByUsername", method = RequestMethod.GET)
	public ModelAndView findUser(@RequestParam String usernameSearch, @RequestParam int censusId,
			@CookieValue("user") String username) {
		ModelAndView result;
		String requestUri = "census/searchByUsername.do?username=" + usernameSearch;

		Collection<String> usernames = censusService.findByUsername(usernameSearch, censusId);

		Census census = censusService.findOne(censusId);
		Date now = new Date();

		Boolean editable;
		editable = census.getTipoCenso().equals("cerrado") && census.getUsername().equals(username)
				&& census.getFechaFinVotacion().after(now);

		Collection<String> userList = census.getVotoPorUsuario().keySet();

		result = new ModelAndView("census/manage");
		result.addObject("requestURI", requestUri);
		result.addObject("usernames", usernames);
		result.addObject("census", census);
		result.addObject("user", userList);
		result.addObject("editable", editable);
		return result;
	}

	// Detalles del censo -----------------------------------------------------

	@RequestMapping(value = "/details", method = RequestMethod.GET)
	public ModelAndView details(@RequestParam int censusId, @CookieValue("user") String username) {
		ModelAndView result;
		Date now = new Date();
		Census census = censusService.findOne(censusId);
		result = createEditModelAndView(census);
		Boolean editable;
		editable = census.getTipoCenso().equals("cerrado") && census.getUsername().equals(username)
				&& census.getFechaFinVotacion().after(now);

		result.addObject("editable", editable);
		return result;
	}

	// Editar un censo para añadir o quitar usuarios y buscarlos --------------

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public ModelAndView edit(@RequestParam int censusId, @CookieValue("user") String username) {
		ModelAndView result = new ModelAndView("census/manage");

		Date now = new Date();
		Boolean editable;

		// Llamada a todos los usuarios del sistema

		Map<String, String> usernamesAndEmails = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		Census census = censusService.findOneByCreator(censusId, username);

		Collection<String> userList = census.getVotoPorUsuario().keySet();
		editable = census.getTipoCenso().equals("cerrado") && census.getUsername().equals(username)
				&& census.getFechaFinVotacion().after(now);
		result.addObject("usernames", usernamesAndEmails.keySet());
		result.addObject("census", census);
		result.addObject("user", userList);
		result.addObject("editable", editable);
		result.addObject("requestURI", "census/edit.do");

		return result;
	}

	// Exportar un censo a .txt -----------------------------------------------

	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public ModelAndView export(@RequestParam int censusId) throws IOException {
		ModelAndView result;
		Census census = censusService.findOne(censusId);
		String typeOS = System.getProperty("os.name");
		String userHomeDir = System.getProperty("user.home");
		File file = null;

		// Hacemos un cambio en el directorio donde se guardará el fichero .txt
		// dependiendo de si estamos trabajando sobre Windows o sobre Linux

		if (typeOS.contains("Windows")) {
			file = new File(userHomeDir + "/Desktop/filename" + censusId + ".txt");
		} else if (typeOS.contains("Linux")) {
			file = new File(userHomeDir + "/Escritorio/filename" + censusId + ".txt");
		} else if (typeOS.contains("Mac")) {
			file = new File(userHomeDir + "/Desktop/filename" + censusId + ".txt");
		}

		// Comprobamos que el txt que vamos a crear no existe ya en este
		// directorio,
		// sino existe, creamos un txt nuevo, en caso de que exista uno, lo
		// sustituimos

		if (!file.exists()) {
			file.createNewFile();
		}

		// Aquí estamos dando el formato que queremos que tenga nuestro .txt

		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.newLine();
		bufferedWriter.write("Details of the census");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.write("Owner: " + census.getUsername());
		bufferedWriter.newLine();
		bufferedWriter.write("Name of vote: " + census.getTituloVotacion());
		bufferedWriter.newLine();
		bufferedWriter.write("Vote number: " + census.getIdVotacion());
		bufferedWriter.newLine();
		bufferedWriter.write("Start date: " + census.getFechaInicioVotacion());
		bufferedWriter.newLine();
		bufferedWriter.write("Finish date: " + census.getFechaFinVotacion());
		bufferedWriter.newLine();
		bufferedWriter.write("---------------------");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.write("Voters: ");
		bufferedWriter.newLine();

		// Todos los usuarios del sistema

		Map<String, String> mapUsers = RESTClient.getMapUSernameAndEmailByJsonAutentication();
		Collection<String> usernames = mapUsers.keySet();

		// Todos los que han votado en un censo

		Collection<String> userList = census.getVotoPorUsuario().keySet();
		if (usernames.size() != 0) {

			// Aquí compruebo que de todos los usuarios disponibles en el
			// sistema, cuál de
			// ellos ha votado ya en el censo

			for (String aux : usernames) {
				for (String voter : userList) {

					// Uno de los usuarios del sistema ya ha votado en dicho
					// censo

					if (aux.equals(voter)) {

						// Obtenemos el mapa del censo, para saber si el usuario
						// ha votado o no

						HashMap<String, Boolean> map = census.getVotoPorUsuario();

						// Añadimos este usuario que ya ha votado al text

						User user = RESTClient.getCertainUserByJsonAuthentication(voter);
						bufferedWriter.newLine();
						bufferedWriter.write("User_Id: " + user.getUId());
						bufferedWriter.newLine();
						bufferedWriter.write("Username: " + user.getUsername());
						bufferedWriter.newLine();
						bufferedWriter.write("Email: " + user.getEmail());
						bufferedWriter.newLine();
						bufferedWriter.write("Genre: " + user.getGenre());
						bufferedWriter.newLine();
						bufferedWriter.write("Autonomous community: " + user.getAutonomousCommunity());
						bufferedWriter.newLine();
						bufferedWriter.write("Age: " + user.getAge());
						bufferedWriter.newLine();
						bufferedWriter.write("Has voted?: " + map.get(voter));
						bufferedWriter.newLine();
						bufferedWriter.write("*****************");
						bufferedWriter.newLine();
						break;
					}
				}
			}
		} else {
			bufferedWriter.write("Nothing to display because there isn't any voters");
		}

		bufferedWriter.close();
		result = new ModelAndView("redirect:getAllCensusByCreador.do");
		return result;
	}

	// Save -------------------------------------------------------------------

	@RequestMapping(value = "/edit", method = RequestMethod.POST, params = "save")
	public ModelAndView save(@Valid Census census, BindingResult binding) {
		ModelAndView result;

		if (binding.hasErrors()) {
			result = createEditModelAndView(census);
		} else {
			try {
				censusService.save(census);
				result = new ModelAndView("redirect:/census/list.do");

			} catch (Throwable oops) {
				result = createEditModelAndView(census, "census.commit.error");
			}
		}

		return result;
	}

	// Nos devuelve una lista con los censos en los que nos podemos registrar -

	@RequestMapping(value = "/getCensusesToRegister", method = RequestMethod.GET)
	public ModelAndView getCensusesToRegister(@CookieValue("user") String username) {
		ModelAndView result = new ModelAndView("census/censosARegistrar");
		Collection<Census> censuses = new ArrayList<Census>();
		censuses = censusService.findCensusesToRegisterByUser(username);
		result.addObject("censuses", censuses);
		result.addObject("misVotaciones", false);
		result.addObject("requestURI", "census/getCensusesToRegister.do");

		return result;
	}
	@RequestMapping(value = "/getFinishedCensus", method = RequestMethod.GET)
	public ModelAndView getFinishedCensus(@CookieValue("user") String user) {
		ModelAndView result = new ModelAndView("census/finishedCensus");
		Collection<Census> censuses = new ArrayList<Census>();
		censuses = censusService.findRecentFinishedCensus(user);
		result.addObject("censuses", censuses);
		result.addObject("requestURI", "census/getFinishedCensus.do");

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	protected ModelAndView createEditModelAndView(Census census) {
		ModelAndView result;

		result = createEditModelAndView(census, null);

		return result;
	}

	protected ModelAndView createEditModelAndView(Census census, String message) {
		ModelAndView result = new ModelAndView("census/create");

		if (census.getId() != 0) {
			result = new ModelAndView("census/details");
		}
		HashMap<String, Boolean> mapa = census.getVotoPorUsuario();
		result.addObject("census", census);
		result.addObject("mapa", mapa);
		result.addObject("message", message);

		return result;

	}

}
