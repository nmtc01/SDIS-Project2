public class Chunk implements java.io.Serializable{
    private String file_id;
    private Integer chunk_no;
    private Integer chunk_size;

    public void setDesired_replication_degree(int desired_replication_degree) {
        this.desired_replication_degree = desired_replication_degree;
    }

    private int desired_replication_degree;
    private byte[] content;

    public Chunk(String file_id, Integer chunk_no, Integer chunk_size, int replication_degree, byte[] content) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.chunk_size = chunk_size;
        this.desired_replication_degree = replication_degree;
        this.content = content;
    }

    public String getFile_id() {
        return file_id;
    }

    public Integer getChunk_no() {
        return chunk_no;
    }

    public byte[] getContent() {
        return this.content;
    }

    public Integer getChunk_size() {
        return chunk_size;
    }

    public int getDesired_replication_degree() {
        return desired_replication_degree;
    }
}
