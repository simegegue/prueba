package services;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



import services.CensusService;
import domain.Census;

@ContextConfiguration(locations = { "classpath:spring/datasource.xml", 
									"classpath:spring/config/packages.xml" })

@RunWith(SpringJUnit4ClassRunner.class)

public class CensusServiceTest {
	
	// Service under test -----------------------

	@Autowired
	private CensusService censusService;
	
	// Tests ------------------------------------
	
	@Test
	public void findAllCensus(){
		Collection<Census> result;
		result = censusService.findAllCensus();
	
		System.out.println("Los censos almacenados son: " + result);
	}

	@Test
	public void createCensus() throws ParseException {
		
		// Inicializamos un censo, inicializando los valores de los atributos -
		
		Census c_1 = censusService.create(1,"DavidElecciones75", "201220150800", "201220152000", "Votación de prueba", "cerrada");
		
		// Inicializamos HashMap donde especificaremos quién ha votado y quién no, introducimos usuarios 
		// y estos los introducimos en el censo creado ------------------------
		
		HashMap<String, Boolean> vpo = c_1.getVotoPorUsuario();
		
		vpo.put("Pepe88", true);
		vpo.put("Ramiro90", false);
		vpo.put("Demetrio76", true);
		
		c_1.setVotoPorUsuario(vpo);
		
		// Mostramos por pantalla el Censo creado ------------------------------
		
		System.out.println(c_1);

	}
	
	@Test
	public void findOneCensus() throws ParseException{
		
		// Inicializamos una variable de tipo Census y buscamos uno con un id en concreto -
		
		Census result;
		
		result = censusService.findOne(2);
		
		System.out.println(result);
	}
	
	@Test
	public void findAllCensuses() throws ParseException{
		
		// Inicializamos una variable como colección de Censuses agrupando por los censos
		// creados por un usuario en concretro y buscamos que nos devuelva una colección de 
		// Censuses -----------------------------------------------------------
		
		Collection<Census> result;
		
		result = censusService.findCensusByCreator("DavidElecciones75");
		
		for(Census census: result){
		
			System.out.println(census);
		}
	}
	
}
