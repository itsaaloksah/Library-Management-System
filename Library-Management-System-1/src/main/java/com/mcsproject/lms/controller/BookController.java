package com.mcsproject.lms.controller;

import com.mcsproject.lms.entity.BookEntity;
import com.mcsproject.lms.repository.BookRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

    @PostMapping("/save-book")
    public String saveBook(@RequestParam String title,
                           @RequestParam String author,
                           @RequestParam String category,
                           @RequestParam Double price,
                           @RequestParam Integer quantity,
                           RedirectAttributes redirectAttributes) {

        BookEntity book = new BookEntity();

        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setPrice(price);
        book.setQuantity(quantity);

        bookRepo.save(book);

        redirectAttributes.addFlashAttribute(
                "success",
                "Book added successfully 📚"
        );

        return "redirect:/add-book";
    }
    
    @GetMapping("/list-books")
    public String listBooks(Model model,
                           HttpSession session) {

        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("books", bookRepo.findAll());

        return "list-books";
    }
    
    @GetMapping("/edit-book/{id}")
    public String editBook(@PathVariable Long id,
                          Model model,
                          HttpSession session) {

        if(session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        BookEntity book = bookRepo.findById(id).orElse(null);

        model.addAttribute("book", book);

        return "edit-book";
    }
    
    @PostMapping("/update-book")
    public String updateBook(@RequestParam Long id,
                             @RequestParam String title,
                             @RequestParam String author,
                             @RequestParam String category,
                             @RequestParam Double price,
                             @RequestParam Integer quantity,
                             RedirectAttributes redirectAttributes) {

        BookEntity book = bookRepo.findById(id).orElse(null);

        if(book != null) {

            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(category);
            book.setPrice(price);
            book.setQuantity(quantity);

            bookRepo.save(book);
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Book updated successfully ✅"
        );

        return "redirect:/list-books";
    }
    
    @GetMapping("/delete-book/{id}")
    public String deleteBook(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {

        bookRepo.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Book deleted successfully 🗑️"
        );

        return "redirect:/list-books";
    }
}