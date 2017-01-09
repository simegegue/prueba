package utilities;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import domain.User;

public class RESTClient {

	/***
	 * M�todo que lee un Json de autenticaci�n y devuelve un map con los
	 * username de todos los usuarios registrados en el sistema como clave y sus
	 * emails como valor para que el admin del censo pueda a�adir usuarios y se
	 * les notifique mediante email.
	 */

	public static Map<String, String> getMapUSernameAndEmailByJsonAutentication() {

		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject("http://auth-egc.azurewebsites.net/api/getUsers", String.class);
		String[] lista = result.split("},");

		Map<String, String> mapUsernamesEmails = new HashMap<String, String>();
		String username = null;
		String email;
		for (@SuppressWarnings("unused")
		String usuario : lista) {

			String[] lista2 = result.split(",");
			for (String campo : lista2) {
				String[] auxList = campo.split(":");
				if (campo.contains("Username")) {
					username = auxList[1];
					username = username.replaceAll("\"", "");
					mapUsernamesEmails.put(username, null);

				}
				if (campo.contains("Email")) {
					email = auxList[1];
					email = email.replaceAll("\"", "");
					mapUsernamesEmails.put(username, email);
				}

			}

		}

		return mapUsernamesEmails;
	}

	public static void main(String[] args) throws IOException {
		Map<String, String> usernamesAndEmails = getMapUSernameAndEmailByJsonAutentication();
		System.out.println(usernamesAndEmails);
	}

	/***
	 * M�todo que lee nos devuelve el Json con la informaci�n de un usuario en
	 * concreto pas�ndole un username, el Json obtenido de autenticaci�n se
	 * leer� para formar un tipo User que ser� lo que se devuelva.
	 */

	public static User getCertainUserByJsonAuthentication(String username) {
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject("http://auth-egc.azurewebsites.net/api/getUser?username=" + username,
				String.class);
		System.out.println(result);
		String[] lista = result.split(",");
		User user = new User();
		for (String field : lista) {
			if (field.contains("U_id")) {
				String[] auxList = field.split(":");
				String uId = auxList[1];
				uId = uId.replaceAll("\"", "");
				System.out.println(uId);
				int uIdConverted = Integer.parseInt(uId);
				user.setUId(uIdConverted);
			}
			if (field.contains("Username")) {
				String[] auxList = field.split(":");
				String usernameUser = auxList[1];
				usernameUser = usernameUser.replaceAll("\"", "");
				user.setUsername(usernameUser);
			}
			if (field.contains("Email")) {
				String[] auxList = field.split(":");
				String email = auxList[1];
				email = email.replaceAll("\"", "");
				user.setEmail(email);
			}
			if (field.contains("Genre")) {
				String[] auxList = field.split(":");
				String genre = auxList[1];
				genre = genre.replaceAll("\"", "");
				user.setGenre(genre);
			}
			if (field.contains("Autonomous_community")) {
				String[] auxList = field.split(":");
				String autonomousCommunity = auxList[1];
				autonomousCommunity = autonomousCommunity.replaceAll("\"", "");
				user.setAutonomousCommunity(autonomousCommunity);
			}
			if (field.contains("Age")) {
				String[] auxList = field.split(":");
				String age = auxList[1];
				String[] age_list = age.split("}");
				String finalAge = age_list[0];

				int ageConverted = Integer.parseInt(finalAge);
				user.setAge(ageConverted);
			}
		}
		System.out.println(user);
		return user;

	}
}
