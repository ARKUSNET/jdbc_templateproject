import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class TestPersonRepository {
    private EmbeddedDatabase embeddedDatabase;

    private JdbcTemplate jdbcTemplate;

    private PersonRepository personRepository;

    @Before
    public void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .addDefaultScripts()
                .setType(EmbeddedDatabaseType.H2)
                .build();

        jdbcTemplate = new JdbcTemplate(embeddedDatabase);

        personRepository = new PersonRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void testFindAll() {
        Assert.assertNotNull(personRepository.findAll());
        Assert.assertEquals(2, personRepository.findAll().size());
    }

    @Test
    public void testFindOne() {
        Assert.assertNull(personRepository.findOne("nonexistent-id"));
        Assert.assertNotNull(personRepository.findOne("jack-daniels"));
    }

    @Test
    public void testSave() {
        Person person = personRepository.save(new Person("Jim Beam", "jimbeam@example.com"));

        Assert.assertNotNull(person);
        Assert.assertNotNull(person.getId());
        Assert.assertEquals("Jim Beam", person.getName());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveInvalid() {
        personRepository.save(new Person());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveConflict() {
        personRepository.save(new Person("Jim Beam", "jackdaniels@example.com"));
    }

    @Test
    public void testUpdate() {
        Person person = jdbcTemplate.queryForObject("select * from person where id = 'jack-daniels'", PersonRepository.ROW_MAPPER);
        person.setName("Johny Walker");

        person = personRepository.save(person);
        Assert.assertNotNull(person);
        Assert.assertNotNull(person.getId());
        Assert.assertEquals("Johny Walker", person.getName());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testUpdateInvalid() {
        Person person = jdbcTemplate.queryForObject("select * from person where id = 'jack-daniels'", PersonRepository.ROW_MAPPER);
        person.setName(null);

        personRepository.save(person);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testUpdateConflict() {
        Person person = jdbcTemplate.queryForObject("select * from person where id = 'jack-daniels'", PersonRepository.ROW_MAPPER);
        person.setEmail("georgedickel@example.com");

        personRepository.save(person);
    }

    @Test
    public void testDelete() {
        Assert.assertEquals(1, personRepository.delete("jack-daniels"));
        Assert.assertEquals(0, personRepository.delete("nonexistent-id"));
    }

    @After
    public void tearDown() {
        embeddedDatabase.shutdown();
    }
}
