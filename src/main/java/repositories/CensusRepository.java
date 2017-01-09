package repositories;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import domain.Census;

@Repository
public interface CensusRepository extends JpaRepository<Census, Integer> {
	
	//Devuelve los censos por orden descendente de fecha
	@Query("select c from Census c order by c.fechaFinVotacion desc")
	Collection<Census> allCensus();
	
	// Buscar un censo de una votación
	
	@Query("select c from Census c where c.idVotacion = ?1")
	public Census findCensusByVote(int idVotacion);
	
	// Buscar un censo por creador
	
	@Query("select c from Census c where c.username = ?1")
	public Collection<Census> findCensusByCreator(String username);
	
	// Devuelve los censos abiertos
	
	@Query("select c from Census c where c.tipoCenso = 'abierto'")
	public Collection<Census> findAllOpenedCensuses();
	
	//Devuelve los censos finalizados en los ultimos dos meses
	@Query("select c from Census c where MONTH(current_date)-MONTH(c.fechaFinVotacion) BETWEEN 0 and 1 and DATE(current_date)>DATE(c.fechaFinVotacion)")
	public Collection<Census> findAllCensusFinishedRecently();
}

