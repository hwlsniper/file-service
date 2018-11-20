package io.choerodon.file.app.service

import io.choerodon.core.exception.CommonException
import io.choerodon.file.IntegrationTestConfiguration
import io.choerodon.file.app.service.impl.FileServiceImpl
import io.minio.MinioClient
import org.apache.poi.util.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.FileSystemResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class FileServiceSpec extends Specification {
    @Autowired
    FileServiceImpl fileService

    private MinioClient minioClient=Mock(MinioClient)

    void setup() {
        fileService.setMinioClient(minioClient)
    }

    def "UploadFile"() {
        given: "参数准备"
        def backetName = "test"
        def fileName = "testUploadFile"
        def file = new File(this.class.getResource('/testFileUpload.txt').toURI())
        FileInputStream input = new FileInputStream(file)
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input))
        def url="xxx/xxx/testFileUpload.txt"
        and: "mock"
        minioClient.bucketExists(_) >> { return true }
        minioClient.getObjectUrl(_, _) >> { return url }
        when: "方法调用"
        def result= fileService.uploadFile(backetName, fileName, multipartFile)
        then: '无异常抛出'
        result == url
        noExceptionThrown()
    }
    def "UploadFile[Exception]"() {
        given: "参数准备"
        def backetName = "test"
        def fileName = "testUploadFile"
        def file = new File(this.class.getResource('/testFileUpload.txt').toURI())
        FileInputStream input = new FileInputStream(file)
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input))
        and: "mock"
        minioClient.bucketExists(_) >> { return false }
        minioClient.setBucketPolicy(_,_,_) >> { throw new Exception("exception") }
        when: "方法调用"
        def result= fileService.uploadFile(backetName, fileName, multipartFile)
        then: '无异常抛出'
        result == null
        noExceptionThrown()
    }
    def "DeleteFile[backetNameNotExist]"() {
        given:"参数准备"
        def backetName="backetName"
        def url="xxx/xxx/testFileUpload.txt"
        and:'mock'
        minioClient.bucketExists(_) >> {return false}
        when:'方法调用'
        fileService.deleteFile(backetName, url)
        then:"抛出异常"
        def e = thrown(CommonException)
        e.getCause().getCode()=="error.backetName.notExist"
    }
    def "DeleteFile[fileNotExist]"() {
        given:"参数准备"
        def backetName="backetName"
        def url="http://127.0.0.1:8888/backetName/"
        and:'mock'
        minioClient.bucketExists(_) >> {return true}
        when:'方法调用'
        fileService.deleteFile(backetName, url)
        then:"抛出异常"
        def e1 = thrown(CommonException)
        e1.getCause().getCode()=="error.file.notExist"

        and:'参数准备'
        def url2="http://127.0.0.1:8888/backetName/fileName"
        minioClient.getObject(_,_) >> {return null}
        when:'方法调用'
        fileService.deleteFile(backetName, url2)
        then:"抛出异常"
        def e2 = thrown(CommonException)
        e2.getCause().getCode()=="error.file.notExist"
    }
    def "DeleteFile"() {
        given:"参数准备"
        def backetName="backetName"
        def url="http://127.0.0.1:8888/backetName/fileName"
        def file = new File(this.class.getResource('/testFileUpload.txt').toURI())
        def is=new FileInputStream(file)
        and:'mock'
        minioClient.bucketExists(_) >> {return true}
        minioClient.getObject(_,_) >> {return is}
        when:'方法调用'
        fileService.deleteFile(backetName, url)
        then:"抛出异常"
        noExceptionThrown()
    }
    def "UploadDocument"() {
        given: "参数准备"
        def backetName = "test"
        def originFileName = "testUploadFile"
        def file = new File(this.class.getResource('/testFileUpload.txt').toURI())
        FileInputStream input = new FileInputStream(file)
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input))
        and: "mock"
        minioClient.bucketExists(_) >> { return true }
        when: "方法调用"
        def result= fileService.uploadDocument(backetName, originFileName, multipartFile)
        then: '无异常抛出'
        result.getEndPoint()!=null
        result.getOriginFileName()==originFileName
        result.getFileName()!=null
        noExceptionThrown()
    }
    def "UploadDocument[Exception]"() {
        given: "参数准备"
        def backetName = "test"
        def originFileName = "testUploadFile"
        def file = new File(this.class.getResource('/testFileUpload.txt').toURI())
        FileInputStream input = new FileInputStream(file)
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input))
        and: "mock"
        minioClient.bucketExists(_) >> { return false }
        minioClient.setBucketPolicy(_,_,_) >> { throw new Exception("exception") }
        when: "方法调用"
        def result= fileService.uploadDocument(backetName, originFileName, multipartFile)
        then: '无异常抛出'
        result==null
        noExceptionThrown()
    }
}
