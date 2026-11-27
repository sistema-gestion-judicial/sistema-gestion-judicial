// Shortens long page lists the way the Tocas design does ("1 2 … 9"): it keeps the first and last pages and
// the ones right next to the current page, and puts a "…" where pages were left out. The arrows stay as they are.
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".pagination").forEach((list) => {
    const pages = [...list.querySelectorAll(".page-item")].filter((item) => !item.querySelector("[aria-label]"));
    if (pages.length <= 5) {
      return;
    }
    const current = Math.max(0, pages.findIndex((item) => item.classList.contains("active")));
    const last = pages.length - 1;
    const keep = (index) => index === 0 || index === last || Math.abs(index - current) <= 1;
    let gapAdded = false;
    pages.forEach((item, index) => {
      if (keep(index)) {
        gapAdded = false;
        return;
      }
      if (!gapAdded) {
        const gap = document.createElement("li");
        gap.className = "page-item is-gap";
        gap.setAttribute("aria-hidden", "true");
        gap.innerHTML = '<span class="page-link">…</span>';
        item.before(gap);
        gapAdded = true;
      }
      item.hidden = true;
    });
  });
});
