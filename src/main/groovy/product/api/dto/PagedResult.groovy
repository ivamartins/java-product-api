package product.api.dto

class PagedResult<T> {
    List<T> content
    Long totalElements
    Integer page
    Integer size
    Integer totalPages

    PagedResult(List<T> content, Long totalElements, Integer page, Integer size) {
        this.content = content
        this.totalElements = totalElements
        this.page = page
        this.size = size
        this.totalPages = (totalElements / size) + ((totalElements % size > 0) ? 1 : 0)
    }
}
