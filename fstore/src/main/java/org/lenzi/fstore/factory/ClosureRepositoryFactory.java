package org.lenzi.fstore.factory;

import org.lenzi.fstore.properties.ManagedProperties;
import org.lenzi.fstore.repository.ClosureRepository;
import org.lenzi.fstore.repository.OracleClosureRepository;
import org.lenzi.fstore.repository.PostgresClosureRepository;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory for creating instances of ClosureRepository.
 * 
 * @author slenzi
 */
@Service
public class ClosureRepositoryFactory implements FactoryBean<ClosureRepository>{

    @Autowired
    private ManagedProperties appProps;
    
	@InjectLogger
	private Logger logger;

	@Override
	public ClosureRepository getObject() throws Exception {
		if(appProps.getDatabaseType().toUpperCase().trim().equals("ORACLE")){
			logger.info("Get " + OracleClosureRepository.class.getName());
			return new OracleClosureRepository();
		}else if(appProps.getDatabaseType().toUpperCase().trim().equals("POSTGRES")){
			logger.info("Get " + PostgresClosureRepository.class.getName());
			return new PostgresClosureRepository();
		}
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		if(appProps.getDatabaseType().toUpperCase().trim().equals("ORACLE")){
			return OracleClosureRepository.class;
		}else if(appProps.getDatabaseType().toUpperCase().trim().equals("POSTGRES")){
			return PostgresClosureRepository.class;
		}
		return null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	

}
