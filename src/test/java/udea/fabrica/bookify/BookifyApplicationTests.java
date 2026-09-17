package udea.fabrica.bookify;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Deshabilitado en local por requerir variables de entorno de BD")
class BookifyApplicationTests {

	@Test
	void contextLoads() {
	}

}
