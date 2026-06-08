package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.BookEntity;
import com.mcsproject.lms.repository.BookRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepo;

    @GetMapping("/add-book")
    public String addBookPage(HttpSession session) {
        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "add-book";
    }

    // CHANGED: Added @ResponseBody to return a status token directly to JavaScript
    @PostMapping("/save-book")
    @ResponseBody
    public String saveBook(@RequestParam String title,
                           @RequestParam String author,
                           @RequestParam String category,
                           @RequestParam Double price,
                           @RequestParam Integer quantity) {

        BookEntity book = new BookEntity();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setPrice(price);
        book.setQuantity(quantity);

        bookRepo.save(book);

        return "SAVED_SUCCESS";
    }
    
    @GetMapping("/list-books")
    public String listBooks(Model model, HttpSession session) {
        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("books", bookRepo.findAll());
        return "list-books";
    }
    
    @GetMapping("/edit-book/{id}")
    public String editBook(@PathVariable Long id, Model model, HttpSession session) {
        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        BookEntity book = bookRepo.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "edit-book";
    }
    
    // CHANGED: Added @ResponseBody to communicate status directly to JavaScript
    @PostMapping("/update-book")
    @ResponseBody
    public String updateBook(@RequestParam Long id,
                             @RequestParam String title,
                             @RequestParam String author,
                             @RequestParam String category,
                             @RequestParam Double price,
                             @RequestParam Integer quantity) {

        BookEntity book = bookRepo.findById(id).orElse(null);
        if(book != null) {
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(category);
            book.setPrice(price);
            book.setQuantity(quantity);
            bookRepo.save(book);
        }

        return "UPDATED_SUCCESS";
    }
    
    // CHANGED: Added @ResponseBody to handle deletion asynchronously 
    @GetMapping("/delete-book/{id}")
    @ResponseBody
    public String deleteBook(@PathVariable Long id) {
        bookRepo.deleteById(id);
        return "DELETED_SUCCESS";
    }
}