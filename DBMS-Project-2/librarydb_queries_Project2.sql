-- 1. For each library member his card number, first, middle and last name along with the number of book copies he ever borrowed. There may be members who didn't ever borrow any book copy.
-- ------------------------------
select card_no, first_name, middle_name, last_name, count(*) as copies_borrowed
from member natural join borrow
group by card_no
union
select card_no, first_name, middle_name, last_name, 0
from member
where card_no not in (select card_no from borrow);
-- ________________________________
-- 2. Members (their card numbers, first, middle and last names) who held a book copy the longest. 
-- There can be one such member or more than one. 
-- Don't take into accout a case that someone borrowed the same book copy again.
-- Don't take into account members who borrowed a book copy and didn't return it yet.
-- --------------------------------
select card_no, first_name, middle_name, last_name
from member natural join borrow
where date_returned is not null and 
      datediff(date_returned, date_borrowed) = (select max(datediff(date_returned, date_borrowed)) from borrow);
-- ________________________________
-- 3. For each book (ISBN and title) the number of copies the library owns.
-- --------------------------------
select ISBN, title, count(*) as num_copies
from book natural join copy
group by ISBN;
-- ________________________________
-- 4. Books (ISBNs and titles), if any, having exactly 3 authors.
-- --------------------------------
select ISBN, title
from book natural join book_author
group by ISBN
having count(*) = 3;
-- ________________________________
-- 5. For each author (ID, first, middle and last name) the number of books he wrote.
-- --------------------------------
select author_id, first_name, middle_name, last_name, count(*) as books_wrote
from author natural join book_author
group by author_id;
-- ________________________________
-- 6. Card number, first, middle and last name of members, if any, who borrowed some book by Chartrand(s). 
-- Remove duplicates from the result.
-- --------------------------------
select distinct card_no, member.first_name, member.middle_name, member.last_name
from member natural join borrow natural join copy natural join book_author, author
where book_author.author_id = author.author_id and author.last_name = "Chartrand";
-- ________________________________
-- 7. Most popular author(s) (their IDs and first, middle and last names) in the library.
-- --------------------------------
select author_id, first_name, middle_name, last_name
from borrow natural join copy natural join book_author natural join author
group by author_id
having count(*) = (select count(*)
                  from borrow natural join copy natural join book_author natural join author
                  group by author_id
                  order by count(*) desc
                  limit 1);
-- ________________________________
-- 8. Card numbers, first, middle, last names and addresses of members whose libray card will expire within the next month.
-- --------------------------------
select card_no, first_name, middle_name, last_name, email_address
from member
where card_exp_date between curdate() and date_add(curdate(), interval 1 month);
-- ________________________________
-- 9. Card numbers, first, middle and last names of members along with the amount of money they owe to the library. 
-- Assume that if a book copy is returned one day after the due date, a member ows 0.25 cents to the library.
-- --------------------------------
select card_no, first_name, middle_name, last_name, 
       sum(case
		   when ((datediff(date_returned, date_borrowed) - (renewals_no + 1) * 14) > 0)
           then (datediff(date_returned, date_borrowed) - (renewals_no + 1) * 14) * .25
           else 0
	   end) as amount_owed
from borrow natural join member
group by card_no
union
select card_no, first_name, middle_name, last_name, 0
from member
where card_no not in (select card_no from borrow);
-- ________________________________
-- 10. The amount of money the library earned (received money for) from late returns.
-- --------------------------------
select sum(amount_paid) as total_amount_earned
from (select
      sum(case
		   when ((datediff(date_returned, date_borrowed) - (renewals_no + 1) * 14) > 0)
           then (datediff(date_returned, date_borrowed) - (renewals_no + 1) * 14) * .25
           else 0
	   end) as amount_paid
       from borrow
       where paid = 1
       group by card_no) as late_returns;
-- ________________________________
-- 11. Members (their card numbers, first, middle and last names) who borrowed more non-fiction books than fiction books.
-- --------------------------------
select card_no, first_name, middle_name, last_name
from member natural join borrow natural join copy natural join book natural join genre
group by card_no
having count(distinct case when type = 1 then ISBN else null end)
	   >
	   count(distinct case when type = 0 then ISBN else null end);
-- ________________________________
-- 12. Name of the most popular publisher(s).
-- --------------------------------
select publisher
from book natural join copy natural join borrow
group by publisher
having count(*) = (select count(*)
                   from book natural join copy natural join borrow
                   group by publisher
                   order by count(*) desc
                   limit 1);
-- ________________________________
-- 13. Members (card numbers, first, middle and last names) who never borrowed any book copy and whose card expired.
-- --------------------------------
select card_no, first_name, middle_name, last_name
from member
where card_no not in (select card_no from borrow) and card_exp_date < curdate();
-- ________________________________
-- 14. The most popular genre(s).
-- --------------------------------
select name
from genre natural join book natural join copy natural join borrow
group by name
having count(*) = (select count(*)
                   from genre natural join book natural join copy natural join borrow
                   group by name
                   order by count(*) desc
                   limit 1);
-- ________________________________
-- 15. For each state, in which some member lives, the most pupular last name(s). 
-- --------------------------------
select state, last_name
from (select state, last_name, count(*) as count
      from member
      group by state, last_name) as t1
where count = (select max(count)
               from (select state, last_name, count(*) as count
                     from member
                     group by state, last_name) as t2
			   where t1.state = t2.state)
order by state;
-- ________________________________
-- 16. Books (ISBNs and titles) that don't have any authors. 
-- --------------------------------
select ISBN, title
from book
where ISBN not in (select ISBN
			       from book_author);
-- ________________________________
-- 17. Members (card numbers) who borrowed the same book more than once (not necessarily the same copy of a book).
-- --------------------------------
select distinct card_no
from borrow natural join copy
group by card_no, ISBN
having count(*) > 1;
-- ________________________________
-- 18. Number of members from Cookeville, TN and from Algood, TN.
-- --------------------------------
select count(*) as num_members
from member
where (city = 'Cookeville' and state = 'TN') or (city = 'Algood' and state = 'TN');
-- ________________________________
-- 19. Card numbers and emails of members who should return a book copy tomorrow. If these members didn't renew their loan twice, then they still have a chance to renew their loan. If they won't renew or return a book tomorrow, then they will be charged for the following day(s).
-- --------------------------------
select distinct card_no, email_address
from member natural join borrow
where date(date_add(date_borrowed, interval 14 * (renewals_no + 1) day)) = curdate() + interval 1 day;
-- ________________________________
-- 20. Condition of a book copy that was borrowed the most often, not necessarily held the longest.
-- --------------------------------
select barcode, comment as copy_condition
from borrow natural join copy
group by barcode
having count(*) = (select count(*)
                   from borrow natural join copy
                   group by barcode
                   order by count(*) desc
                   limit 1);
-- ________________________________