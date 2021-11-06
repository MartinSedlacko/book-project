/*
 * The book project lets a user keep track of different books they would like to read, are currently
 * reading, have read or did not finish.
 * Copyright (C) 2021  Karan Kumar
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.karankumar.bookproject.backend.controller;

import com.karankumar.bookproject.backend.dto.BookDto;
import com.karankumar.bookproject.backend.dto.BookPatchDto;
import com.karankumar.bookproject.backend.model.Book;
import com.karankumar.bookproject.backend.model.account.User;
import com.karankumar.bookproject.backend.service.BookService;
import com.karankumar.bookproject.backend.service.PredefinedShelfService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookControllerTest {
    private final BookController bookController;
    private final BookService mockedBookService;
    private final PredefinedShelfService mockedPredefinedShelfService;

    private final User fakeUser;
    private static final String BOOK_NOT_FOUND_ERROR_MESSAGE = "Could not find book with ID %d";

    BookControllerTest() {
        mockedBookService = mock(BookService.class);
        mockedPredefinedShelfService = mock(PredefinedShelfService.class);
        ModelMapper mockedModelMapper = mock(ModelMapper.class);
        fakeUser = new User(1L, "fakeUser@karankumar.com", "fakePass",
                true, new HashSet<>());
        bookController = new BookController(
                mockedBookService,
                mockedPredefinedShelfService,
                mockedModelMapper
        );
    }

    @Test
    void all_returnsEmptyList_whenNoBooksExist() {
        when(mockedBookService.findAll()).thenReturn(new ArrayList<>());

        assertThat(bookController.all().size()).isZero();
    }

    @Test
    void all_returnsNonEmptyList_whenBooksExist() {
        // given
        List<Book> books = new ArrayList<>();
        books.add(new Book());
        books.add(new Book());

        // when
        when(mockedBookService.findAll()).thenReturn(books);

        // then
        assertThat(bookController.all().size()).isEqualTo(books.size());
    }

    @Test
    void findById_returnsBook_ifPresent() {
        Book book = new Book();
        when(mockedBookService.findById(any(Long.class)))
                .thenReturn(Optional.of(book));

        assertThat(bookController.findById(0L)).isEqualTo(book);
    }

    @Test
    void findById_returnsNotFound_ifBookIsEmpty() {
        when(mockedBookService.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> bookController.findById(0L));
    }

    @Test
    // TODO: finish writing this test
    void findByShelf_returnsNotFound_ifBookDoesNotExist() {
//        when(mockedBookService.findByShelfAndTitleOrAuthor(
//                any(Shelf.class),
//                any(String.class),
//                any(String.class))
//        ).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

//        assertThatExceptionOfType(BookNotFoundException.class)
//                .isThrownBy(bookController.findByShelf(new CustomShelf(), "title", "author"));
    }

    @Test
    // TODO: finish writing this test
    void delete_returnsNotFound_ifBookDoesNotExist() {
        when(mockedBookService.findById(any(Long.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

//        assertThatExceptionOfType(BookNotFoundException.class)
//                .isThrownBy(bookController.delete(1L));
    }

    @Test
    void givenBookDto_WhenAddBook_ThenReturnBook(){
        BookDto bookDto = new BookDto();
        bookDto.setTitle("Fake book");
        Book book = new Book();
        book.setTitle("Fake book");

        when(bookController.convertToBook(bookDto)).thenReturn(book);
        when(mockedBookService.save(book)).thenReturn(Optional.of(book));

        Optional<Book> result = bookController.addBook(bookDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get().getTitle(),book.getTitle())
        );

        verify(mockedBookService, times(1)).save(book);
    }

    @Test
    void givenBookIdAndBookPatchDto_WhenUpdate_ThenReturnUpdatedBook(){
        Long id = 1L;
        Book bookToUpdate = new Book();
        bookToUpdate.setTitle("Fake bookToUpdate");
        Book updatedBook = new Book();
        updatedBook.setTitle("Fake updatedBook");
        BookPatchDto bookPatchDto = new BookPatchDto();
        bookPatchDto.setTitle("Fake updatedBook");

        when(mockedBookService.findById(id)).thenReturn(Optional.of(bookToUpdate));
        when(mockedBookService.updateBook(bookToUpdate, bookPatchDto)).thenReturn(updatedBook);

        Book result = bookController.update(id, bookPatchDto);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(updatedBook.getTitle(), result.getTitle())
        );

        verify(mockedBookService, times(1)).findById(id);
        verify(mockedBookService, times(1)).updateBook(bookToUpdate, bookPatchDto);
    }

    @Test
    void givenNotExistingBookId_WhenUpdate_ThenThrowResponseStatusException(){
        Long id = 1L;
        BookPatchDto bookPatchDto = new BookPatchDto();

        when(mockedBookService.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException thrownException =
                assertThrows(ResponseStatusException.class, () -> bookController.update(id, bookPatchDto));

        assertAll(
                () -> assertNotNull(thrownException),
                () -> assertEquals(HttpStatus.NOT_FOUND, thrownException.getStatus()),
                () -> assertEquals(
                        thrownException.getStatus() + " \"" + String.format(BOOK_NOT_FOUND_ERROR_MESSAGE , id) +"\"",
                        thrownException.getMessage())
        );

        verify(mockedBookService, times(1)).findById(id);
    }

    @Test
    void givenBookId_WhenDelete_ThenCallDeleteMethod(){
        Long id = 1L;
        Book bookToDelete = new Book();

        when(mockedBookService.findById(id)).thenReturn(Optional.of(bookToDelete));

        bookController.delete(id);

        verify(mockedBookService, times(1)).findById(id);
        verify(mockedBookService, times(1)).delete(bookToDelete);
    }

    @Test
    void givenInvalidBookId_WhenDelete_ThenThrowResponseStatusException(){
        Long id = 1L;

        when(mockedBookService.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException thrownException =
                assertThrows(ResponseStatusException.class, () -> bookController.delete(id));

        assertAll(
                () -> assertNotNull(thrownException),
                () -> assertEquals(HttpStatus.NOT_FOUND, thrownException.getStatus()),
                () -> assertEquals(
                        thrownException.getStatus() + " \"" + String.format(BOOK_NOT_FOUND_ERROR_MESSAGE , id) +"\"",
                        thrownException.getMessage())
        );
    }
}
