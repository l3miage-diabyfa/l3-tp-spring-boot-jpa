package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import fr.uga.l3miage.library.service.mock.BookServiceMockImpl;
import jakarta.validation.constraints.Null;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/authors", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping()
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public AuthorDTO author(@PathVariable("id") Long id) throws EntityNotFoundException {
        try {
            var author =  authorService.get(id);
                if(author ==null || author.getId()== null){
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }
                return authorMapper.entityToDTO(author);
            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            
        }
                
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody AuthorDTO author) {
        try{
            if(author==null|| author.fullName()==null
            ||author.fullName().trim()==""){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            Author author1=authorMapper.dtoToEntity(author);
            Author newAuthor=authorService.save(author1);
            AuthorDTO authorDTO=authorMapper.entityToDTO(newAuthor);
             return authorDTO;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
     
    }

    @PutMapping("/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO  author, @PathVariable("id") Long id) throws EntityNotFoundException {
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
        
        if(author.id()==id){

            Author authorEntity=authorMapper.dtoToEntity(author);
            
            Author authorUpdated=authorService.update(authorEntity);
            return authorMapper.entityToDTO(authorUpdated);

        }
        else{
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

     
    
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.BAD_REQUEST)
    public void deleteAuthor(@PathVariable("id") Long id) throws EntityNotFoundException, DeleteAuthorException {
        if(id==null || id < 0){
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        try{
            authorService.delete(id);
        }
        catch(EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
                   
    }
@GetMapping("/{id}/books")
    public Collection<BookDTO> books(@PathVariable("id") Long authorId) throws EntityNotFoundException {
        Author author =authorService.get(authorId);
      Collection<Book> books=author.getBooks();
        return books.stream()
        .map(booksMapper::entityToDTO)
        .toList();
    }

}
