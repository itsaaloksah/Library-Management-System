package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.BookEntity;
import com.mcsproject.lms.repository.BookRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepo;

    // UPDATED: Handles both Sidebar Clicking (AJAX) and Hard Browser Reloads (F5)
    @GetMapping("/add-book")
    public String addBookPage(HttpServletRequest request, HttpSession session) {
        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        
        // Check if the request is an asynchronous AJAX fetch
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "add-book"; // Return just the raw HTML form fragment
        }
        
        // User pressed F5: Flag the initial view path and boot up via dashboard container shell
        session.setAttribute("loadInitialView", "/add-book");
        return "redirect:/dashboard";
    }

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
    
    // UPDATED: Handles Sidebar Clicking, Global Search inputs, and F5 hard reloads safely
    @GetMapping("/list-books")
    public String listBooks(HttpServletRequest request, HttpSession session, Model model) {
        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("books", bookRepo.findAll());
        
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return "list-books"; // Return just the raw inventory table rows fragment
        }
        
        // User pressed F5: Bounce them cleanly through dashboard layout framework
        session.setAttribute("loadInitialView", "/list-books");
        return "redirect:/dashboard";
    }

    // ADDED: Endpoint for fully functional dynamic dashboard/global case-insensitive search box
    @GetMapping("/books/search")
    public String executeGlobalSearch(@RequestParam("query") String query, Model model) {
        if (query == null || query.trim().length() < 2) {
            model.addAttribute("books", bookRepo.findAll());
        } else {
            List<BookEntity> filteredBooks = bookRepo.searchBooksGlobal(query.trim());
            model.addAttribute("books", filteredBooks);
        }
        return "list-books"; // Directly targets and re-renders inventory rows fragment
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
    
    @GetMapping("/delete-book/{id}")
    @ResponseBody
    public String deleteBook(@PathVariable Long id) {
        bookRepo.deleteById(id);
        return "DELETED_SUCCESS";
    }
}